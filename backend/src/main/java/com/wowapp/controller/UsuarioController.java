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

    // Endpoint para registrar un nuevo usuario
    @PostMapping("/registro")
    public String registrar(@RequestBody Usuario usuario) {
        // Verifica si ya existe un usuario con el mismo email
        if (usuarioService.obtenerPorEmail(usuario.getEmail()).isPresent()) {
            return "Ya existe un usuario con ese email.";
        }
        // Registra al usuario si no existe
        usuarioService.registrar(usuario);
        return "Registro exitoso.";
    }

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
