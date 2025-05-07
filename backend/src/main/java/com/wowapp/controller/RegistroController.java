package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class RegistroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Instancia del codificador de contraseñas BCrypt
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Clase interna para representar la solicitud de registro
    public static class RegistroRequest {
        @Email // Validación para asegurar que el campo sea un correo electrónico válido
        public String email;

        @NotBlank // Validación para asegurar que el campo no esté vacío
        public String nombre_usuario;

        @NotBlank // Validación para asegurar que el campo no esté vacío
        public String password;

        @NotBlank // Validación para asegurar que el campo no esté vacío
        public String confirmar;
    }

    // Endpoint para registrar un nuevo usuario
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(@RequestBody RegistroRequest request, HttpSession session) {
        if (!request.password.equals(request.confirmar)) {
            return ResponseEntity.badRequest().body("Las contraseñas no coinciden.");
        }

        if (usuarioRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado.");
        }

        if (usuarioRepository.findByNombreUsuario(request.nombre_usuario).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya está registrado.");
        }

        Usuario usuarioPendiente = new Usuario();
        usuarioPendiente.setEmail(request.email);
        usuarioPendiente.setNombreUsuario(request.nombre_usuario);
        usuarioPendiente.setPasswordHash(passwordEncoder.encode(request.password));
        usuarioPendiente.setTipo("web");
        usuarioPendiente.setEsVerificado(false);

        // Guarda en sesión como usuario pendiente
        session.setAttribute("usuario_pendiente", usuarioPendiente);
        session.setAttribute("origen", "registro");

        return ResponseEntity.status(302).header("Location", "/verificacion.html").build();
    }
}
