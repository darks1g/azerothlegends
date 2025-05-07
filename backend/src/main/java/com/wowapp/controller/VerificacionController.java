package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificacionController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/verificar-codigo")
    public ResponseEntity<Map<String, Object>> verificarCodigo(@RequestBody Map<String, String> datos, HttpSession session) {
        String codigoIngresado = datos.get("codigo");

        Map<String, Object> verificacion = (Map<String, Object>) session.getAttribute("verificacion");
        Usuario usuarioPendiente = (Usuario) session.getAttribute("usuario_pendiente");
        String origen = (String) session.getAttribute("origen");

        if (verificacion == null || usuarioPendiente == null || origen == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sesión de verificación inválida."));
        }

        long expiracion = (long) verificacion.get("expira");
        String codigoCorrecto = String.valueOf(verificacion.get("codigo"));
        String email = (String) verificacion.get("email");

        if (!email.equals(usuarioPendiente.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "El correo no coincide."));
        }

        if (Instant.now().getEpochSecond() > expiracion) {
            return ResponseEntity.status(403).body(Map.of("error", "El código ha expirado."));
        }

        if (!codigoCorrecto.equals(codigoIngresado)) {
            return ResponseEntity.status(403).body(Map.of("error", "Código incorrecto."));
        }

        // ✅ Verificación superada
        if (origen.equals("registro")) {
            usuarioPendiente.setEsVerificado(true);
            usuarioRepository.save(usuarioPendiente);
        }

        session.setAttribute("usuario", usuarioPendiente);
        session.removeAttribute("usuario_pendiente");
        session.removeAttribute("verificacion");
        session.removeAttribute("origen");

        return ResponseEntity.ok(Map.of("success", true));
    }
}
