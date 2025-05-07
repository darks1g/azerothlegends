package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (CORS)
public class AuthController {

    @Autowired
    private UsuarioService usuarioService; // Servicio para manejar la lógica de negocio relacionada con usuarios

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            // Intenta registrar un nuevo usuario
            Usuario nuevo = usuarioService.registrar(usuario);
            return ResponseEntity.ok("Usuario registrado correctamente."); // Respuesta exitosa
        } catch (RuntimeException e) {
            // Manejo de errores en caso de que ocurra un problema durante el registro
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        // Extrae el email y la contraseña del cuerpo de la solicitud
        String email = datos.get("email");
        String password = datos.get("password");

        // Verifica si las credenciales son válidas
        boolean valido = usuarioService.verificarCredenciales(email, password);
        if (valido) {
            // Si las credenciales son válidas, obtiene el usuario por su email
            Optional<Usuario> usuario = usuarioService.obtenerPorEmail(email);
            return ResponseEntity.ok(usuario.get()); // Retorna el usuario (puedes ajustar qué datos retornar)
        } else {
            // Si las credenciales no son válidas, retorna un error de autorización
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        }
    }
}
