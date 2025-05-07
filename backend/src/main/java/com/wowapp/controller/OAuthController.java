package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import com.wowapp.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    // ID del cliente proporcionado por Blizzard
    @Value("${blizzard.client-id}")
    private String clientId;

    // Región configurada para la API de Blizzard
    @Value("${blizzard.region}")
    private String region;

    // URI de redirección configurada para el flujo OAuth
    @Value("${blizzard.redirect-uri}")
    private String redirectUri;

    private final OAuthService oauthService;
    private final UsuarioRepository usuarioRepository;

    // Constructor del controlador, inyecta dependencias necesarias
    public OAuthController(OAuthService oauthService, UsuarioRepository usuarioRepository) {
        System.out.println("Intentando construir OAuthController...");
        this.oauthService = oauthService;
        this.usuarioRepository = usuarioRepository;
    }

    // Método de prueba para confirmar si Spring carga el controlador
    @GetMapping("/test")
    public String test() {
        return "Funciona";
    }

    // Redirige al usuario a la página de autorización de Battle.net
    @GetMapping("/battlenet")
    public void redirigir(HttpServletResponse response, HttpSession session) throws IOException {
        // Genera un estado aleatorio para la sesión (opcional, para validación)
        String state = java.util.UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);

        // Construye la URL de autorización de Battle.net
        String url = "https://" + region + ".battle.net/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=openid+wow.profile" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        // Redirige al usuario a la URL generada
        response.sendRedirect(url);
    }

    // Maneja el callback de OAuth después de la autorización
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code, HttpSession session) {
        // Procesa el código de autorización y obtiene el battletag del usuario
        String battletag = oauthService.procesarOAuthCallback(code);

        // Busca al usuario en la base de datos por su battletag
        Optional<Usuario> usuarioOpt = usuarioRepository.findByBattletag(battletag);

        // Si el usuario no existe, lo crea y lo guarda en la base de datos
        Usuario usuario = usuarioOpt.orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setBattletag(battletag);
            nuevo.setTipo("battlenet");
            nuevo.setEmail(battletag.replace("#", "_") + "@battlenet.fake"); // Genera un email ficticio
            nuevo.setEsVerificado(true); // Marca al usuario como verificado
            return usuarioRepository.save(nuevo);
        });

        // Guarda al usuario en la sesión
        session.setAttribute("usuario", usuario);

        // Redirige al usuario a la página principal
        return ResponseEntity.status(302).header("Location", "/").build();
    }
}
