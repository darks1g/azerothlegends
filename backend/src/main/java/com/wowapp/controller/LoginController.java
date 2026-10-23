package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final long PRUEBA_VALIDA_SEGUNDOS = 300;

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ConcurrentHashMap<String, Long> noncesConsumidos = new ConcurrentHashMap<>();

    @Value("${app.verification-secret:}")
    private String verificationSecret;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public static class LoginRequest {
        @Email
        public String email;

        @NotBlank
        public String password;
    }

    public static class VerificacionRequest {
        @Email
        public String email;

        @NotBlank
        public String timestamp;

        @NotBlank
        public String nonce;

        @NotBlank
        public String signature;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Correo no registrado."));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.password, usuario.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta."));
        }

        session.removeAttribute("usuario");
        session.removeAttribute("usuario_pendiente");
        session.removeAttribute("registro_pendiente_expira");
        session.setAttribute("login_pendiente_email", usuario.getEmail());
        session.setAttribute("login_pendiente_expira", Instant.now().getEpochSecond() + PRUEBA_VALIDA_SEGUNDOS);

        return ResponseEntity.ok(Map.of("email", usuario.getEmail()));
    }

    @PostMapping("/login/confirmar")
    public ResponseEntity<Map<String, String>> confirmarVerificacion(
            @Valid @RequestBody VerificacionRequest request, HttpServletRequest servletRequest, HttpSession session) {
        long ahora = Instant.now().getEpochSecond();
        String emailPendiente = (String) session.getAttribute("login_pendiente_email");
        Long expira = (Long) session.getAttribute("login_pendiente_expira");
        String emailRegistro = null;
        Long expiraRegistro = (Long) session.getAttribute("registro_pendiente_expira");
        Object usuarioPendiente = session.getAttribute("usuario_pendiente");
        if (usuarioPendiente instanceof Usuario usuario) {
            emailRegistro = usuario.getEmail();
        }

        String emailEsperado = emailRegistro != null ? emailRegistro : emailPendiente;
        if (emailEsperado == null || !emailEsperado.equalsIgnoreCase(request.email)
                || (emailRegistro != null && (expiraRegistro == null || ahora > expiraRegistro))
                || (emailRegistro == null && (expira == null || ahora > expira))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "La sesión pendiente de verificación ha expirado."));
        }

        if (!pruebaValida(request, ahora)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "La verificación no es válida o ya se ha utilizado."));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(emailEsperado);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No se encontró el usuario verificado."));
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setEsVerificado(true);
        usuarioRepository.save(usuario);

        servletRequest.changeSessionId();
        session.setAttribute("usuario", usuario);
        session.removeAttribute("login_pendiente_email");
        session.removeAttribute("login_pendiente_expira");
        session.removeAttribute("usuario_pendiente");
        session.removeAttribute("registro_pendiente_expira");
        session.removeAttribute("origen");

        return ResponseEntity.ok(Map.of("email", usuario.getEmail()));
    }

    private boolean pruebaValida(VerificacionRequest request, long ahora) {
        if (verificationSecret == null || verificationSecret.isBlank()
                || request.nonce == null || !request.nonce.matches("[a-f0-9]{32}")
                || request.signature == null || !request.signature.matches("[a-f0-9]{64}")) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(request.timestamp);
        } catch (NumberFormatException e) {
            return false;
        }
        if (timestamp > ahora || ahora - timestamp > PRUEBA_VALIDA_SEGUNDOS) {
            return false;
        }

        String mensaje = request.email + "\n" + request.timestamp + "\n" + request.nonce;
        byte[] esperado;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(verificationSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            esperado = mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo verificar la firma de autenticación.", e);
        }

        byte[] recibido = HexFormat.of().parseHex(request.signature);
        if (!MessageDigest.isEqual(esperado, recibido)) {
            return false;
        }

        noncesConsumidos.entrySet().removeIf(entry -> entry.getValue() < ahora);
        return noncesConsumidos.putIfAbsent(request.nonce, timestamp + PRUEBA_VALIDA_SEGUNDOS) == null;
    }

}
