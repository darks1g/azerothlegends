package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Lazy;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.List;

import com.wowapp.repository.ReinoRepository;
import com.wowapp.model.Reino;
import com.wowapp.repository.PersonajeRepository;

@Service
public class ApiService {

    @Value("${blizzard.client-id}")
    private String clientId;

    @Value("${blizzard.client-secret}")
    private String clientSecret;

    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    // Método para obtener el token de autenticación desde la API de Blizzard
    public String obtenerToken() {
        if (token != null) {
            return token; // Si el token ya existe, se reutiliza
        }

        HttpHeaders headers = new HttpHeaders();
        String credentials = clientId + ":" + clientSecret;
        String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + base64Creds);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://eu.battle.net/oauth/token", request, Map.class);

        token = (String) response.getBody().get("access_token");
        return token;
    }

    // Método para obtener información de un personaje desde la API de Blizzard
    public Personaje obtenerPersonajeDesdeAPI(String nombre, String reino, String region, VersionJuego versionJuego) {
        List<String> namespaces;
        String reinoFormateado = reino.toLowerCase().replace(" ", "-");

        switch (versionJuego) {
            case retail -> namespaces = List.of("profile-" + region);
            case classic -> namespaces = List.of("profile-classic-" + region);
            case classic_era -> namespaces = List.of("profile-classic1x-" + region);
            default -> throw new IllegalArgumentException("Versión de juego no reconocida.");
        }

        for (String ns : namespaces) {
            try {
                String url = String.format(
                        "https://%s.api.blizzard.com/profile/wow/character/%s/%s?namespace=%s&locale=es_ES",
                        region,
                        reinoFormateado,
                        nombre.toLowerCase(),
                        ns);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + obtenerToken());
                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
                Map<String, Object> datos = response.getBody();

                if (datos == null)
                    continue;

                Personaje p = new Personaje();
                p.setId(((Number) datos.get("id")).longValue());
                p.setNombre(nombre);
                p.setReino(reino);
                p.setRegion(region);
                p.setNivel((Integer) datos.get("level"));

                if (datos.containsKey("character_class"))
                    p.setClase(((Map<?, ?>) datos.get("character_class")).get("name").toString());

                if (datos.containsKey("race"))
                    p.setRaza(((Map<?, ?>) datos.get("race")).get("name").toString());

                if (datos.containsKey("gender"))
                    p.setGenero(((Map<?, ?>) datos.get("gender")).get("type").toString());

                p.setVersionJuego(versionJuego);
                return p;

            } catch (Exception e) {
                // Se ignoran las excepciones para intentar con el siguiente namespace
            }
        }

        throw new RuntimeException("No se pudo encontrar el personaje en ningún namespace.");
    }

    @Autowired
    private ReinoRepository reinoRepository;

    // Método para poblar la base de datos con los reinos obtenidos desde la API
    public void poblarReinosDesdeAPI(String region, VersionJuego version) {
        String versionSuffix = switch (version) {
            case retail -> "";
            case classic_era -> "classic1x-";
            case classic -> "classic-";
        };

        String namespace = "dynamic-" + versionSuffix + region;
        String url = String.format(
                "https://%s.api.blizzard.com/data/wow/realm/index?namespace=%s&locale=es_ES",
                region, namespace);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            List<Map<String, Object>> reinos = (List<Map<String, Object>>) response.getBody().get("realms");

            for (Map<String, Object> reinoData : reinos) {
                Reino r = new Reino();
                r.setId(((Number) reinoData.get("id")).longValue());
                r.setNombre((String) reinoData.get("name"));
                r.setSlug((String) reinoData.get("slug"));
                r.setRegion(region);
                r.setVersionJuego(version);

                reinoRepository.save(r);
            }

            System.out.println("Reinos poblados: " + region + " / " + version);
        } catch (Exception e) {
            System.err.println("Error al poblar reinos para " + region + " / " + version + ": " + e.getMessage());
        }
    }

    @Autowired
    private EstadisticasService estadisticasService;

    @Autowired
    private PersonajeRepository personajeRepository;

    // Método para obtener y guardar estadísticas de un personaje
    public void obtenerYGuardarEstadisticas(Personaje personaje) {

        // Verificar si el personaje fue actualizado recientemente
        if (!necesitaActualizacion(personaje)) {
            System.out.println("Personaje " + personaje.getNombre()
                    + " fue actualizado recientemente. Se omite llamada a la API.");
            return;
        }

        String reinoSlug = personaje.getReino().toLowerCase().replace(" ", "-");
        String ns = switch (personaje.getVersionJuego()) {
            case retail -> "profile-" + personaje.getRegion();
            case classic -> "profile-classic-" + personaje.getRegion();
            case classic_era -> "profile-classic1x-" + personaje.getRegion();
        };
        String url = String.format(
                "https://%s.api.blizzard.com/profile/wow/character/%s/%s/statistics?namespace=%s&locale=es_ES",
                personaje.getRegion(),
                reinoSlug,
                personaje.getNombre().toLowerCase(),
                ns);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> datos = response.getBody();

            estadisticasService.guardarEstadisticas(personaje, datos);

            System.out.println("Estadísticas actualizadas para: " + personaje.getNombre());
        } catch (Exception e) {
            System.err.println(
                    "No se pudieron obtener estadísticas para " + personaje.getNombre() + ": " + e.getMessage());
        }
    }

    @Autowired
    @Lazy
    private TalentoRetailService talentoRetailService;

    @Autowired
    @Lazy
    private TalentoClassicService talentoClassicService;

    // Método para obtener y guardar talentos de un personaje
    public void obtenerYGuardarTalentos(Personaje personaje) {
        String reinoSlug = personaje.getReino().toLowerCase().replace(" ", "-");

        String ns = switch (personaje.getVersionJuego()) {
            case retail -> "profile-" + personaje.getRegion();
            case classic -> "profile-classic-" + personaje.getRegion();
            case classic_era -> "profile-classic1x-" + personaje.getRegion();
        };

        String endpoint = "specializations";

        String url = String.format(
                "https://%s.api.blizzard.com/profile/wow/character/%s/%s/%s?namespace=%s&locale=es_ES",
                personaje.getRegion(),
                reinoSlug,
                personaje.getNombre().toLowerCase(),
                endpoint,
                ns);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> datos = response.getBody();

            switch (personaje.getVersionJuego()) {
                case retail -> talentoRetailService.guardarTalentos(personaje, datos);
                case classic, classic_era -> talentoClassicService.guardarTalentos(personaje, datos);
            }

            System.out.println("Talentos guardados para: " + personaje.getNombre());
        } catch (Exception e) {
            System.err
                    .println("No se pudieron obtener talentos para " + personaje.getNombre() + ": " + e.getMessage());
        }
    }

    @Autowired
    @Lazy
    private EquipoPersonajeService equipoPersonajeService;

    // Método para obtener y guardar el equipo de un personaje
    public void obtenerYGuardarEquipo(Personaje personaje) {
        String reinoSlug = personaje.getReino().toLowerCase().replace(" ", "-");
        String ns;
        String url;

        switch (personaje.getVersionJuego()) {
            case retail -> {
                ns = "profile-" + personaje.getRegion();
                url = String.format(
                        "https://%s.api.blizzard.com/profile/wow/character/%s/%s/equipment?namespace=%s&locale=es_ES",
                        personaje.getRegion(), reinoSlug, personaje.getNombre().toLowerCase(), ns);
            }
            case classic -> {
                ns = "profile-classic-" + personaje.getRegion();
                url = String.format(
                        "https://%s.api.blizzard.com/profile/wow/character/%s/%s/equipment?namespace=%s&locale=es_ES",
                        personaje.getRegion(), reinoSlug, personaje.getNombre().toLowerCase(), ns);
            }
            case classic_era -> {
                ns = "profile-classic1x-" + personaje.getRegion();
                url = String.format(
                        "https://%s.api.blizzard.com/profile/wow/character/%s/%s/equipment?namespace=%s&locale=es_ES",
                        personaje.getRegion(), reinoSlug, personaje.getNombre().toLowerCase(), ns);
            }
            default -> {
                System.out.println("Versión no soportada para equipo: " + personaje.getVersionJuego());
                return;
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> datos = response.getBody();

            equipoPersonajeService.guardarEquipo(personaje, datos);

        } catch (Exception e) {
            System.err.println("Error obteniendo equipo para " + personaje.getNombre() + ": " + e.getMessage());
        }
    }

    public String obtenerIconoItem(int itemId) {
        String url = String.format(
            "https://%s.api.blizzard.com/data/wow/media/item/%d?namespace=static-%s&locale=es_ES",
            "eu", itemId, "eu" // Usa el region aquí si lo deseas dinámico
        );
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> jsonMap = response.getBody();
            if (jsonMap == null) return null;
    
            List<Map<String, Object>> assets = (List<Map<String, Object>>) jsonMap.get("assets");
            for (Map<String, Object> asset : assets) {
                if ("icon".equals(asset.get("key"))) {
                    String iconUrl = asset.get("value").toString();
                    return iconUrl.substring(iconUrl.lastIndexOf("/") + 1, iconUrl.lastIndexOf("."));
                }
            }
    
        } catch (Exception e) {
            System.err.println("No se pudo obtener icono para itemId=" + itemId + ": " + e.getMessage());
        }
    
        return null;
    }
    
    public boolean necesitaActualizacion(Personaje personaje) {
        return personaje.getFechaActualizacion() == null ||
               personaje.getFechaActualizacion().isBefore(LocalDateTime.now().minusMinutes(5));
    }

    public void actualizarPersonaje(Personaje personaje) {
        if (!necesitaActualizacion(personaje)) {
            System.out.println("El personaje " + personaje.getNombre() + " no necesita actualización.");
            return;
        }
    
        try {
            obtenerYGuardarEstadisticas(personaje);
            obtenerYGuardarEquipo(personaje);
            obtenerYGuardarTalentos(personaje);
    
            personaje.setFechaActualizacion(LocalDateTime.now());
            personajeRepository.save(personaje);
    
            System.out.println("Actualización completa para " + personaje.getNombre());
        } catch (Exception e) {
            System.err.println("Error al actualizar el personaje " + personaje.getNombre() + ": " + e.getMessage());
        }
    }
    
    public String obtenerIconoDeSpell(int spellId) {
        String url = String.format(
            "https://eu.api.blizzard.com/data/wow/media/spell/%d?namespace=static-eu&locale=es_ES",
            spellId
        );
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + obtenerToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
    
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            Map<String, Object> datos = response.getBody();
            List<Map<String, Object>> assets = (List<Map<String, Object>>) datos.get("assets");
            for (Map<String, Object> asset : assets) {
                if ("icon".equals(asset.get("key"))) {
                    String urlIcono = asset.get("value").toString();
                    return urlIcono.substring(urlIcono.lastIndexOf("/") + 1, urlIcono.lastIndexOf(".")); // solo el nombre
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo ícono del spellId=" + spellId + ": " + e.getMessage());
        }
    
        return null;
    }
    

}
