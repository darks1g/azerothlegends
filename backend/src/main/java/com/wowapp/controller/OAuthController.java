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

    @Value("${blizzard.client-id}")
    private String clientId;

    @Value("${blizzard.region}")
    private String region;

    @Value("${blizzard.redirect-uri}")
    private String redirectUri;

    private final OAuthService oauthService;
    private final UsuarioRepository usuarioRepository;

    public OAuthController(OAuthService oauthService, UsuarioRepository usuarioRepository) {
        System.out.println("⚠️ Intentando construir OAuthController...");
        this.oauthService = oauthService;
        this.usuarioRepository = usuarioRepository;
    }

    // 🔍 Método de prueba para confirmar si Spring carga el controlador
    @GetMapping("/test")
    public String test() {
        return "✅ Funciona";
    }

    @GetMapping("/battlenet")
    public void redirigir(HttpServletResponse response, HttpSession session) throws IOException {
        String state = java.util.UUID.randomUUID().toString(); // valor aleatorio
        session.setAttribute("oauth_state", state); // opcional: guardar para validarlo luego

        String url = "https://" + region + ".battle.net/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=openid+wow.profile" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code, HttpSession session) {
        String battletag = oauthService.procesarOAuthCallback(code);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByBattletag(battletag);

        Usuario usuario = usuarioOpt.orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setBattletag(battletag);
            nuevo.setTipo("battlenet");
            nuevo.setEmail(battletag.replace("#", "_") + "@battlenet.fake");
            nuevo.setEsVerificado(true);
            return usuarioRepository.save(nuevo);
        });

        session.setAttribute("usuario", usuario);
        return ResponseEntity.status(302).header("Location", "/").build();
    }
}
