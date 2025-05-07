package com.wowapp.controller;

import com.wowapp.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PerfilController {

    @GetMapping(value = "/perfil-menu", produces = "text/html; charset=UTF-8")
    public String perfilMenu(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return """
                <ul class="perfil-menu">
                  <li><a href="/login">Iniciar sesión</a></li>
                  <li><a href="/registro">Registrarse</a></li>
                </ul>
                """;
        }

        return """
            <ul class="perfil-menu">
              <li><a href="/zona">Mi zona</a></li>
              <li><a href="/chats">Chats</a></li>
              <li><a href="/logout">Cerrar sesión</a></li>
            </ul>
            """;
    }
}
