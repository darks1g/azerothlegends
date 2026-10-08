package com.wowapp.service;

import com.wowapp.exception.ApiException;
import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.model.Reino;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.repository.ReinoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ApiService {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Value("${blizzard.client-id}")
    private String clientId;

    @Value("${blizzard.client-secret}")
    private String clientSecret;

    // Región usada para los datos estáticos (iconos) y la lista de reinos
    @Value("${blizzard.region:eu}")
    private String regionApi;

    // Minutos que deben pasar antes de volver a pedir los datos de un personaje a Blizzard
    @Value("${app.refresco-minutos:5}")
    private long refrescoMinutos;

    private final RestTemplate restTemplate = crearRestTemplate();

    private String token;
    private Instant tokenExpira = Instant.EPOCH;

    // Cachés en memoria de iconos ("version:id" -> nombre del icono, o "" si no existe)
    private final Map<String, String> cacheIconosItem = new ConcurrentHashMap<>();
    private final Map<String, String> cacheIconosSpell = new ConcurrentHashMap<>();

    @Autowired
    private ReinoRepository reinoRepository;

    @Autowired
    private PersonajeRepository personajeRepository;

    @Autowired
    private EstadisticasService estadisticasService;

    @Autowired
    @Lazy
    private TalentoRetailService talentoRetailService;

    @Autowired
    @Lazy
    private TalentoClassicService talentoClassicService;

    @Autowired
    @Lazy
    private EquipoPersonajeService equipoPersonajeService;

    private static RestTemplate crearRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    // ------------------------------------------------------------------
    // Autenticación
    // ------------------------------------------------------------------

    // Devuelve un token válido; lo reutiliza hasta que está a punto de caducar
    public synchronized String obtenerToken() {
        if (token != null && Instant.now().isBefore(tokenExpira)) {
            return token;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://" + regionApi + ".battle.net/oauth/token", new HttpEntity<>(body, headers), Map.class);
            Map<String, Object> datos = response.getBody();
            if (datos == null || !(datos.get("access_token") instanceof String)) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Blizzard no devolvió un token de acceso.");
            }
            token = (String) datos.get("access_token");
            long segundos = datos.get("expires_in") instanceof Number n ? n.longValue() : 3600;
            tokenExpira = Instant.now().plusSeconds(Math.max(60, segundos - 120));
            return token;
        } catch (HttpClientErrorException e) {
            log.error("Blizzard rechazó las credenciales ({}). Revisa BLIZZARD_CLIENT_ID y BLIZZARD_CLIENT_SECRET.",
                    e.getStatusCode().value());
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "No se pudo autenticar con Blizzard. Revisa las credenciales del servidor.");
        } catch (RestClientException e) {
            log.error("No se pudo contactar con Blizzard para obtener el token: {}", e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "No se pudo contactar con Blizzard. Inténtalo de nuevo en un momento.");
        }
    }

    private synchronized void invalidarToken() {
        token = null;
        tokenExpira = Instant.EPOCH;
    }

    // ------------------------------------------------------------------
    // Utilidades de URL y peticiones
    // ------------------------------------------------------------------

    private String sufijo(VersionJuego version) {
        return switch (version) {
            case retail -> "";
            case classic -> "classic-";
            case classic_era -> "classic1x-";
        };
    }

    private String nsPerfil(VersionJuego version, String region) {
        return "profile-" + sufijo(version) + region;
    }

    private String nsEstatico(VersionJuego version, String region) {
        return "static-" + sufijo(version) + region;
    }

    private String nsDinamico(VersionJuego version, String region) {
        return "dynamic-" + sufijo(version) + region;
    }

    private String etiqueta(VersionJuego version) {
        return switch (version) {
            case retail -> "Retail";
            case classic -> "Classic (progresión)";
            case classic_era -> "Classic Era";
        };
    }

    // Construye la URL codificando bien tildes y caracteres especiales de los nombres
    private URI uriApi(String region, String ruta, String namespace) {
        return UriComponentsBuilder.fromUriString("https://" + region + ".api.blizzard.com" + ruta)
                .queryParam("namespace", namespace)
                .queryParam("locale", "es_ES")
                .build()
                .encode()
                .toUri();
    }

    // GET autenticado; si el token ha caducado pide uno nuevo y reintenta una vez
    private Map<String, Object> getJson(URI url) {
        for (int intento = 0; intento < 2; intento++) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(obtenerToken());
            try {
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                        Map.class);
                return (Map<String, Object>) response.getBody();
            } catch (HttpClientErrorException.Unauthorized e) {
                invalidarToken();
                if (intento == 1) {
                    throw e;
                }
            }
        }
        return null;
    }

    private Map<String, Object> obtenerPerfil(Personaje personaje, String endpoint) {
        String slugReino = personaje.getReino().toLowerCase().replace(" ", "-");
        URI url = uriApi(personaje.getRegion(),
                "/profile/wow/character/" + slugReino + "/" + personaje.getNombre().toLowerCase() + "/" + endpoint,
                nsPerfil(personaje.getVersionJuego(), personaje.getRegion()));
        return getJson(url);
    }

    // ------------------------------------------------------------------
    // Personaje
    // ------------------------------------------------------------------

    public Personaje obtenerPersonajeDesdeAPI(String nombre, String reino, String region, VersionJuego versionJuego) {
        String slugReino = reino.trim().toLowerCase().replace(" ", "-");
        String nombreMin = nombre.trim().toLowerCase();
        URI url = uriApi(region, "/profile/wow/character/" + slugReino + "/" + nombreMin,
                nsPerfil(versionJuego, region));

        Map<String, Object> datos;
        try {
            datos = getJson(url);
        } catch (HttpClientErrorException.NotFound e) {
            throw noEncontrado(nombre, reino, versionJuego);
        } catch (HttpClientErrorException e) {
            log.warn("Blizzard respondió {} al buscar {}", e.getStatusCode().value(), url);
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Blizzard respondió con un error (" + e.getStatusCode().value() + "). Inténtalo más tarde.");
        } catch (RestClientException e) {
            log.warn("Fallo de red al buscar {}: {}", url, e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "No se pudo contactar con Blizzard. Inténtalo de nuevo en un momento.");
        }

        if (datos == null || !(datos.get("id") instanceof Number)) {
            throw noEncontrado(nombre, reino, versionJuego);
        }

        Personaje p = new Personaje();
        p.setId(((Number) datos.get("id")).longValue());
        // El nombre oficial llega con su mayúscula correcta
        p.setNombre(datos.get("name") instanceof String n ? n : nombre.trim());
        p.setReino(reino.trim());
        p.setRegion(region);
        p.setNivel(datos.get("level") instanceof Number n ? n.intValue() : null);

        if (datos.get("character_class") instanceof Map<?, ?> m && m.get("name") != null)
            p.setClase(m.get("name").toString());

        if (datos.get("race") instanceof Map<?, ?> m && m.get("name") != null)
            p.setRaza(m.get("name").toString());

        if (datos.get("gender") instanceof Map<?, ?> m && m.get("type") != null)
            p.setGenero(m.get("type").toString());

        p.setVersionJuego(versionJuego);
        return p;
    }

    private ApiException noEncontrado(String nombre, String reino, VersionJuego version) {
        return new ApiException(HttpStatus.NOT_FOUND,
                "No se encontró a «" + nombre.trim() + "» en " + reino.trim() + " (" + etiqueta(version)
                        + "). Revisa el nombre, el reino y la versión; Blizzard tampoco muestra personajes que llevan"
                        + " mucho tiempo sin conectarse.");
    }

    // ------------------------------------------------------------------
    // Reinos
    // ------------------------------------------------------------------

    public void poblarReinosDesdeAPI(String region, VersionJuego version) {
        URI url = uriApi(region, "/data/wow/realm/index", nsDinamico(version, region));
        try {
            Map<String, Object> body = getJson(url);
            List<Map<String, Object>> reinos = body == null ? null : (List<Map<String, Object>>) body.get("realms");
            if (reinos == null) {
                log.warn("Blizzard no devolvió reinos para {} / {}", region, version);
                return;
            }

            // Los IDs de reino se repiten entre versiones del juego: se separan por rangos
            long offset = switch (version) {
                case retail -> 0L;
                case classic_era -> 1_000_000L;
                case classic -> 2_000_000L;
            };

            List<Reino> nuevos = new ArrayList<>();
            for (Map<String, Object> reinoData : reinos) {
                if (!(reinoData.get("id") instanceof Number id)) {
                    continue;
                }
                String slug = reinoData.get("slug") instanceof String s ? s : null;
                Object n = reinoData.get("name");
                String nombre = n instanceof String s ? s
                        : (n instanceof Map<?, ?> m && m.get("es_ES") instanceof String es ? es : slug);
                if (slug == null) {
                    continue;
                }

                Reino r = new Reino();
                r.setId(offset + id.longValue());
                r.setNombre(nombre != null ? nombre : slug);
                r.setSlug(slug);
                r.setRegion(region);
                r.setVersionJuego(version);
                nuevos.add(r);
            }
            reinoRepository.saveAll(nuevos);
            log.info("Reinos poblados: {} / {} ({})", region, version, nuevos.size());
        } catch (Exception e) {
            log.error("Error al poblar reinos para {} / {}: {}", region, version, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Estadísticas, talentos y equipo
    // ------------------------------------------------------------------

    public void obtenerYGuardarEstadisticas(Personaje personaje) {
        try {
            Map<String, Object> datos = obtenerPerfil(personaje, "statistics");
            if (datos != null) {
                estadisticasService.guardarEstadisticas(personaje, datos);
                log.info("Estadísticas actualizadas para {}", personaje.getNombre());
            }
        } catch (Exception e) {
            log.warn("No se pudieron obtener estadísticas para {}: {}", personaje.getNombre(), e.getMessage());
        }
    }

    public void obtenerYGuardarTalentos(Personaje personaje) {
        try {
            Map<String, Object> datos = obtenerPerfil(personaje, "specializations");
            if (datos == null) {
                return;
            }
            switch (personaje.getVersionJuego()) {
                case retail -> talentoRetailService.guardarTalentos(personaje, datos);
                case classic, classic_era -> talentoClassicService.guardarTalentos(personaje, datos);
            }
        } catch (Exception e) {
            log.warn("No se pudieron obtener talentos para {}: {}", personaje.getNombre(), e.getMessage());
        }
    }

    public void obtenerYGuardarEquipo(Personaje personaje) {
        try {
            Map<String, Object> datos = obtenerPerfil(personaje, "equipment");
            if (datos != null) {
                equipoPersonajeService.guardarEquipo(personaje, datos);
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el equipo de {}: {}", personaje.getNombre(), e.getMessage());
        }
    }

    // Imagen del personaje (render). Si Blizzard no la tiene, simplemente no se muestra.
    public void obtenerYGuardarImagen(Personaje personaje) {
        try {
            Map<String, Object> datos = obtenerPerfil(personaje, "character-media");
            if (datos == null || !(datos.get("assets") instanceof List<?> assets)) {
                return;
            }
            String completa = null;
            String recuadro = null;
            String avatar = null;
            for (Object o : assets) {
                if (o instanceof Map<?, ?> a && a.get("key") instanceof String clave
                        && a.get("value") instanceof String valor) {
                    switch (clave) {
                        case "main-raw" -> completa = valor;
                        case "inset" -> recuadro = valor;
                        case "avatar" -> avatar = valor;
                        default -> { }
                    }
                }
            }
            String url = completa != null ? completa : (recuadro != null ? recuadro : avatar);
            if (url != null) {
                personaje.setImagenUrl(url);
            }
        } catch (Exception e) {
            log.info("Sin imagen para {}: {}", personaje.getNombre(), e.getMessage());
        }
    }

    public boolean necesitaActualizacion(Personaje personaje) {
        return personaje.getFechaActualizacion() == null
                || personaje.getFechaActualizacion().isBefore(LocalDateTime.now().minusMinutes(refrescoMinutos));
    }

    public void actualizarPersonaje(Personaje personaje) {
        if (!necesitaActualizacion(personaje)) {
            log.info("{} se actualizó hace poco; se omite la llamada a Blizzard.", personaje.getNombre());
            return;
        }

        log.info("Actualizando personaje {}...", personaje.getNombre());
        try {
            obtenerYGuardarEstadisticas(personaje);
            obtenerYGuardarEquipo(personaje);
            obtenerYGuardarTalentos(personaje);
            obtenerYGuardarImagen(personaje);

            personaje.setFechaActualizacion(LocalDateTime.now());
            personajeRepository.save(personaje);
            log.info("Actualización completa para {}", personaje.getNombre());
        } catch (Exception e) {
            log.error("Error al actualizar el personaje {}: {}", personaje.getNombre(), e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Iconos (con caché en memoria)
    // ------------------------------------------------------------------

    public String obtenerIconoItem(int itemId) {
        return obtenerIconoItem(itemId, VersionJuego.retail);
    }

    public String obtenerIconoItem(int itemId, VersionJuego version) {
        return obtenerIcono("item", itemId, version, cacheIconosItem);
    }

    public String obtenerIconoDeSpell(int spellId) {
        return obtenerIconoDeSpell(spellId, VersionJuego.retail);
    }

    public String obtenerIconoDeSpell(int spellId, VersionJuego version) {
        return obtenerIcono("spell", spellId, version, cacheIconosSpell);
    }

    private String obtenerIcono(String tipo, int id, VersionJuego version, Map<String, String> cache) {
        String clave = version + ":" + id;
        String enCache = cache.get(clave);
        if (enCache != null) {
            return enCache.isEmpty() ? null : enCache;
        }

        // Primero el namespace de la versión; si no está, el de retail (muchos IDs antiguos coinciden)
        List<VersionJuego> intentos = version == VersionJuego.retail
                ? List.of(VersionJuego.retail)
                : List.of(version, VersionJuego.retail);

        String icono = null;
        boolean falloTemporal = false;
        for (VersionJuego v : intentos) {
            String resultado = pedirIcono(tipo, id, v);
            if (resultado == null) {
                falloTemporal = true;
            } else if (!resultado.isEmpty()) {
                icono = resultado;
                break;
            }
        }

        // Solo se recuerda el "no existe" si fue una respuesta 404 definitiva
        if (icono != null) {
            cache.put(clave, icono);
        } else if (!falloTemporal) {
            cache.put(clave, "");
        }
        return icono;
    }

    // Devuelve el icono, "" si no existe (404) o null si falló por otra causa
    private String pedirIcono(String tipo, int id, VersionJuego version) {
        URI url = uriApi(regionApi, "/data/wow/media/" + tipo + "/" + id, nsEstatico(version, regionApi));
        try {
            Map<String, Object> json = getJson(url);
            if (json == null || !(json.get("assets") instanceof List<?> assets)) {
                return "";
            }
            for (Object o : assets) {
                if (o instanceof Map<?, ?> asset && "icon".equals(asset.get("key")) && asset.get("value") != null) {
                    String valor = asset.get("value").toString();
                    int barra = valor.lastIndexOf('/');
                    int punto = valor.lastIndexOf('.');
                    if (barra >= 0 && punto > barra) {
                        return valor.substring(barra + 1, punto);
                    }
                }
            }
            return "";
        } catch (HttpClientErrorException.NotFound e) {
            return "";
        } catch (Exception e) {
            log.warn("No se pudo obtener el icono de {} {} ({}): {}", tipo, id, version, e.getMessage());
            return null;
        }
    }
}
