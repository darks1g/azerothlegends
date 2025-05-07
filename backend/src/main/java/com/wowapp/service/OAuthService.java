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

    public String procesarOAuthCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUrl = "https://" + region + ".battle.net/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        HttpEntity<String> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenRequest, Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

        String profileUrl = "https://" + region + ".battle.net/oauth/userinfo";
        ResponseEntity<Map> profileResponse = restTemplate.exchange(profileUrl, HttpMethod.GET, profileRequest, Map.class);

        Map<String, Object> profile = profileResponse.getBody();
        System.out.println("🔍 Perfil devuelto por Blizzard: " + profile);

        Object battleTagObject = profile.get("battletag");
        if (battleTagObject == null) {
            System.err.println("❌ El campo 'battletag' no se encontró en el perfil: " + profile);
            return null;
        }

        return battleTagObject.toString();
    }
}
