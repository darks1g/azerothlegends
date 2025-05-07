package com.wowapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class OAuthService {

    @Value("${blizzard.client-id}")
    private String clientId;

    @Value("${blizzard.client-secret}")
    private String clientSecret;

    @Value("${blizzard.redirect-uri}")
    private String redirectUri;

    @Value("${blizzard.region}")
    private String region;

    // Método para procesar el callback de OAuth y obtener el BattleTag del usuario
    public String procesarOAuthCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // URL para obtener el token de acceso
        String tokenUrl = "https://" + region + ".battle.net/oauth/token";

        // Configuración de los encabezados para la solicitud del token
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Cuerpo de la solicitud para obtener el token
        String body = "grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        // Realizar la solicitud para obtener el token de acceso
        HttpEntity<String> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenRequest, Map.class);

        // Extraer el token de acceso de la respuesta
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // Configuración de los encabezados para la solicitud del perfil del usuario
        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

        // URL para obtener la información del perfil del usuario
        String profileUrl = "https://" + region + ".battle.net/oauth/userinfo";
        ResponseEntity<Map> profileResponse = restTemplate.exchange(profileUrl, HttpMethod.GET, profileRequest,
                Map.class);

        // Obtener el perfil del usuario de la respuesta
        Map<String, Object> profile = profileResponse.getBody();
        System.out.println("Perfil devuelto por Blizzard: " + profile);

        // Verificar si el campo 'battletag' está presente en el perfil
        Object battleTagObject = profile.get("battletag");
        if (battleTagObject == null) {
            System.err.println("El campo 'battletag' no se encontró en el perfil: " + profile);
            return null;
        }

        // Retornar el BattleTag del usuario
        return battleTagObject.toString();
    }
}
