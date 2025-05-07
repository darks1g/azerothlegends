package com.wowapp.controller;

import com.wowapp.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PerfilController {

  // Método que genera un menú de perfil en formato HTML dependiendo del estado de la sesión
  @GetMapping(value = "/perfil-menu", produces = "text/html; charset=UTF-8")
  public String perfilMenu(HttpSession session) {
    // Obtiene el objeto "usuario" de la sesión
    Usuario usuario = (Usuario) session.getAttribute("usuario");

    // Si no hay un usuario en la sesión, muestra opciones para iniciar sesión o registrarse
    if (usuario == null) {
      return """
        <ul class="perfil-menu">
          <li><a href="/login">Iniciar sesión</a></li>
          <li><a href="/registro">Registrarse</a></li>
        </ul>
        """;
    }

    // Si hay un usuario en la sesión, muestra opciones relacionadas con su perfil
    return """
      <ul class="perfil-menu">
        <li><a href="/zona">Mi zona</a></li>
        <li><a href="/chats">Chats</a></li>
        <li><a href="#" id="cerrarSesion">Cerrar sesión</a></li>
      </ul>
      """;
  }
}
