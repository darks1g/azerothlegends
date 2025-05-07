package com.wowapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionController {

    @GetMapping("/sesion-usuario")
    public Map<String, Object> verificarSesion(HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("autenticado", session.getAttribute("usuario") != null);
        return respuesta;
    }
}
