package com.wowapp.controller;

import com.wowapp.model.Usuario;
import com.wowapp.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Value("${blizzard.client-id}")
    private String clientId;

    @Value("${blizzard.client-secret}")
    private String clientSecret;

    private final String redirectUri = "http://azerothlegends.sytes.net/oauth/callback";
    private final String region = "eu";

    private final UsuarioRepository usuarioRepository;

    public OAuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/battlenet")
    public void redirigirABattleNet(HttpServletResponse response) throws IOException {
        String url = "https://" + region + ".battle.net/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=openid+wow.profile" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code";
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> recibirCallback(@RequestParam("code") String code, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = "https://" + region + ".battle.net/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectUri;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
        String accessToken = (String) response.getBody().get("access_token");

        // Obtener el perfil del usuario
        HttpHeaders perfilHeaders = new HttpHeaders();
        perfilHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> perfilRequest = new HttpEntity<>(perfilHeaders);

        String perfilUrl = "https://" + region + ".battle.net/oauth/userinfo";
        ResponseEntity<Map> perfilResponse = restTemplate.exchange(perfilUrl, HttpMethod.GET, perfilRequest, Map.class);
        String battletag = (String) perfilResponse.getBody().get("battle_tag");

        // Registrar o encontrar al usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByBattletag(battletag);
        Usuario usuario = usuarioOpt.orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setBattletag(battletag);
            nuevo.setTipo("battlenet");
            nuevo.setEmail(battletag.replace("#", "_") + "@battlenet.fake");
            nuevo.setEsVerificado(true);
            return usuarioRepository.save(nuevo);
        });

        // Guardar en sesión
        session.setAttribute("usuario", usuario);

        // Redirigir al inicio o a donde desees
        return ResponseEntity.status(302).header("Location", "/").build();
    }
}
