package com.wowapp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import com.wowapp.service.UsuarioService;

import com.wowapp.model.Usuario;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Endpoint para iniciar sesión
    @PostMapping("/login")
    public String login(@RequestBody Usuario usuario) {
        // Verifica las credenciales del usuario
        if (usuarioService.verificarCredenciales(usuario.getEmail(), usuario.getPasswordHash())) {
            return "Login correcto.";
        }
        // Retorna un mensaje de error si las credenciales son incorrectas
        return "Usuario o contraseña incorrectos.";
    }
}
