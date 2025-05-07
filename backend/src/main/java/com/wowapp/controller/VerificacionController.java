package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class VerificacionController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> datos, HttpSession session) {
        String email = datos.get("email");
        String codigoIngresado = datos.get("codigo");
        String origen = datos.get("origen");

        // Recuperar la verificación PHP desde la sesión si existe
        Map<String, Object> verificacion = (Map<String, Object>) session.getAttribute("verificacion");

        if (verificacion == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sesión de verificación inválida."));
        }

        String codigoCorrecto = String.valueOf(verificacion.get("codigo"));
        String emailVerificado = (String) verificacion.get("email");
        long expiracion = (long) verificacion.get("expira");

        if (!email.equals(emailVerificado)) {
            return ResponseEntity.status(403).body(Map.of("error", "El correo no coincide."));
        }

        if (Instant.now().getEpochSecond() > expiracion) {
            return ResponseEntity.status(403).body(Map.of("error", "El código ha expirado."));
        }

        if (!codigoCorrecto.equals(codigoIngresado)) {
            return ResponseEntity.status(403).body(Map.of("error", "Código incorrecto."));
        }

        if (origen.equals("registro")) {
            // Registro: obtener el usuario pendiente de la sesión
            Usuario usuarioPendiente = (Usuario) session.getAttribute("usuario_pendiente");
            if (usuarioPendiente == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario pendiente no encontrado."));
            }
            usuarioPendiente.setEsVerificado(true);
            usuarioRepository.save(usuarioPendiente);
            session.setAttribute("usuario", usuarioPendiente);
        } else {
            // Login: buscar el usuario por email
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado."));
            }
            session.setAttribute("usuario", usuarioOpt.get());
        }

        // Limpieza de sesión
        session.removeAttribute("verificacion");
        session.removeAttribute("usuario_pendiente");

        return ResponseEntity.ok(Map.of("success", true));
    }
}
