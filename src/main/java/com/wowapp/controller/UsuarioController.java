package com.wowapp.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import com.wowapp.service.UsuarioService;

import com.wowapp.model.Usuario;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public String registrar(@RequestBody Usuario usuario) {
        if (usuarioService.obtenerPorEmail(usuario.getEmail()).isPresent()) {
            return "❌ Ya existe un usuario con ese email.";
        }
        usuarioService.registrar(usuario);
        return "✅ Registro exitoso.";
    }

    @PostMapping("/login")
    public String login(@RequestBody Usuario usuario) {
        if (usuarioService.verificarCredenciales(usuario.getEmail(), usuario.getPasswordHash())) {
            return "✅ Login correcto.";
        }
        return "❌ Usuario o contraseña incorrectos.";
    }
}
