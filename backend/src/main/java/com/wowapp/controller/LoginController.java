package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Clase interna para representar la solicitud de login
    public static class LoginRequest {
        @Email
        public String email;

        @NotBlank
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Correo no registrado.");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.password, usuario.getPasswordHash())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta.");
        }

        // Almacenar como usuario pendiente hasta verificar código
        session.setAttribute("usuario_pendiente", usuario);
        session.setAttribute("origen", "login");

        return ResponseEntity.status(302).header("Location", "/verificacion.html").build();
    }

}
