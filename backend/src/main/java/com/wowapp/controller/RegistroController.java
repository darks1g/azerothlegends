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
        // Verifica si las contraseñas coinciden
        if (!request.password.equals(request.confirmar)) {
            return ResponseEntity.badRequest().body("Las contraseñas no coinciden.");
        }

        // Verifica si el correo ya está registrado
        if (usuarioRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado.");
        }

        // Crea un nuevo usuario y establece sus propiedades
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email);
        usuario.setNombreUsuario(request.nombre_usuario);
        usuario.setPasswordHash(passwordEncoder.encode(request.password)); // Encripta la contraseña
        usuario.setTipo("web"); // Tipo de usuario
        usuario.setEsVerificado(false); // El usuario no está verificado inicialmente

        // Guarda el usuario en la base de datos
        usuarioRepository.save(usuario);
         // Iniciar sesión automáticamente
        session.setAttribute("usuario", usuario);

        return ResponseEntity.status(302).header("Location", "/").build();
    }
}
