package com.wowapp.controller;

import com.wowapp.dto.TalentoDTO;
import com.wowapp.exception.ApiException;
import com.wowapp.model.EquipoPersonaje;
import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.EquipoPersonajeRepository;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.service.EstadisticasService;
import com.wowapp.service.PersonajeService;
import com.wowapp.service.TalentoClassicService;
import com.wowapp.service.TalentoRetailService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personajes")
public class PersonajeController {

    private final PersonajeRepository personajeRepository;
    private final EquipoPersonajeRepository equipoPersonajeRepository;
    private final EstadisticasService estadisticasService;
    private final TalentoRetailService talentoRetailService;
    private final TalentoClassicService talentoClassicService;
    private final PersonajeService personajeService;

    public PersonajeController(
        PersonajeRepository personajeRepository,
        EquipoPersonajeRepository equipoPersonajeRepository,
        EstadisticasService estadisticasService,
        TalentoRetailService talentoRetailService,
        TalentoClassicService talentoClassicService,
        PersonajeService personajeService
    ) {
        this.personajeRepository = personajeRepository;
        this.equipoPersonajeRepository = equipoPersonajeRepository;
        this.estadisticasService = estadisticasService;
        this.talentoRetailService = talentoRetailService;
        this.talentoClassicService = talentoClassicService;
        this.personajeService = personajeService;
    }

    // Busca un personaje (en la base de datos o en Blizzard) y devuelve sus datos básicos
    @PostMapping("/buscar")
    public Map<String, Object> buscarPersonaje(@RequestBody Map<String, String> datos) {
        String nombre = datos.get("nombre");
        String reino = datos.get("reino");
        String region = datos.getOrDefault("region", "eu");
        VersionJuego version = parsearVersion(datos.get("version"));

        if (nombre == null || nombre.isBlank() || reino == null || reino.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Indica el nombre del personaje y el reino.");
        }

        Personaje personaje = personajeService.obtenerYGuardarPersonaje(nombre, reino, region, version);
        return resumen(personaje);
    }

    // Todos los datos del personaje para la ficha. Si no está guardado (por ejemplo, un enlace
    // compartido), se descarga de Blizzard; si lleva un rato sin actualizarse, se refresca.
    @GetMapping("/detalles")
    public Map<String, Object> obtenerDetallesPersonaje(
            @RequestParam String nombre,
            @RequestParam String reino,
            @RequestParam String region,
            @RequestParam String version) {

        VersionJuego versionJuego = parsearVersion(version);
        Personaje personaje = personajeService.obtenerYGuardarPersonaje(nombre, reino, region, versionJuego);

        Map<String, Object> json = new LinkedHashMap<>(resumen(personaje));
        json.put("genero", personaje.getGenero());
        json.put("especializacion", personaje.getEspecializacion());
        json.put("heroe", personaje.getHeroe());
        // La imagen se sirve desde nuestro servidor (recortada); el "v" evita usar una copia antigua del navegador
        String urlImagen = personaje.getImagenUrl();
        json.put("imagen", urlImagen == null ? null
                : "/api/imagenes/" + personaje.getId() + "?v=" + Integer.toHexString(urlImagen.hashCode()));
        json.put("actualizado", personaje.getFechaActualizacion() == null ? null
                : personaje.getFechaActualizacion().atZone(java.time.ZoneId.systemDefault()).toInstant().toString());
        json.put("estadisticas", estadisticasService.obtenerEstadisticasParaVista(personaje));
        json.put("equipo", equipoParaVista(personaje));

        if (versionJuego == VersionJuego.retail) {
            List<TalentoDTO> talentos = talentoRetailService.obtenerTalentosParaVista(personaje);
            json.put("talentosClase", talentos.stream().filter(t -> "class".equals(t.getTipo())).toList());
            json.put("talentosSpec", talentos.stream().filter(t -> "spec".equals(t.getTipo())).toList());
            json.put("talentosHero", talentos.stream().filter(t -> "hero".equals(t.getTipo())).toList());
        } else {
            json.put("talentos", talentoClassicService.obtenerTalentosParaVista(personaje));
        }

        return json;
    }

    // Listado paginado de los personajes guardados, con filtros opcionales
    @GetMapping
    public Map<String, Object> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String clase,
            @RequestParam(required = false) String version,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano) {

        Specification<Personaje> filtro = Specification.where(null);

        if (nombre != null && !nombre.isBlank()) {
            String patron = "%" + nombre.trim().toLowerCase() + "%";
            filtro = filtro.and((root, query, cb) -> cb.like(cb.lower(root.<String>get("nombre")), patron));
        }
        if (clase != null && !clase.isBlank()) {
            String claseMin = clase.trim().toLowerCase();
            filtro = filtro.and((root, query, cb) -> cb.equal(cb.lower(root.<String>get("clase")), claseMin));
        }
        if (version != null && !version.isBlank()) {
            VersionJuego v = parsearVersion(version);
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("versionJuego"), v));
        }

        int tamanoSeguro = Math.min(Math.max(tamano, 1), 48);
        Page<Personaje> resultado = personajeRepository.findAll(filtro,
                PageRequest.of(Math.max(pagina, 0), tamanoSeguro, Sort.by(Sort.Direction.DESC, "fechaActualizacion")));

        List<Map<String, Object>> contenido = new ArrayList<>();
        for (Personaje p : resultado.getContent()) {
            Map<String, Object> fila = new LinkedHashMap<>(resumen(p));
            fila.put("especializacion", p.getEspecializacion());
            fila.put("heroe", p.getHeroe());
            contenido.add(fila);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("contenido", contenido);
        respuesta.put("pagina", resultado.getNumber());
        respuesta.put("totalPaginas", resultado.getTotalPages());
        respuesta.put("total", resultado.getTotalElements());
        return respuesta;
    }

    // ---- utilidades ----

    private VersionJuego parsearVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Falta indicar la versión del juego.");
        }
        try {
            return VersionJuego.valueOf(version.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Versión del juego no reconocida: " + version);
        }
    }

    // Solo los datos públicos del personaje (nunca la entidad completa, que enlaza con el usuario)
    private Map<String, Object> resumen(Personaje p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nombre", p.getNombre());
        m.put("reino", p.getReino());
        m.put("region", p.getRegion());
        m.put("nivel", p.getNivel());
        m.put("clase", p.getClase());
        m.put("raza", p.getRaza());
        m.put("versionJuego", p.getVersionJuego());
        return m;
    }

    private List<Map<String, Object>> equipoParaVista(Personaje personaje) {
        List<Map<String, Object>> equipo = new ArrayList<>();
        for (EquipoPersonaje e : equipoPersonajeRepository.findByPersonajeId(personaje.getId())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("slot", e.getSlot());
            m.put("itemId", e.getItemId());
            m.put("nombreItem", e.getNombreItem());
            m.put("ilvl", e.getIlvl());
            m.put("icono", e.getIcono());
            m.put("calidad", e.getCalidad());
            m.put("bonus", e.getBonus());
            equipo.add(m);
        }
        return equipo;
    }
}
