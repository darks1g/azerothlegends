package com.wowapp.controller;

import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.repository.EquipoPersonajeRepository;
import com.wowapp.service.PersonajeService;
import com.wowapp.service.EstadisticasService;
import com.wowapp.service.TalentoRetailService;
import com.wowapp.service.TalentoClassicService;
import com.wowapp.dto.TalentoDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    // Endpoint para buscar un personaje
    @PostMapping("/buscar")
    public ResponseEntity<Personaje> buscarPersonaje(@RequestBody Map<String, String> datos) {
        String nombre = datos.get("nombre");
        String reino = datos.get("reino");
        String region = datos.get("region");
        String versionStr = datos.get("version").toLowerCase();

        VersionJuego version = VersionJuego.valueOf(versionStr);

        Personaje personaje = personajeService.obtenerYGuardarPersonaje(nombre, reino, region, version);
        return ResponseEntity.ok(personaje);
    }

    // Endpoint para obtener todos los detalles del personaje para el frontend
    @GetMapping("/detalles")
public ResponseEntity<?> obtenerDetallesPersonaje(
        @RequestParam String nombre,
        @RequestParam String reino,
        @RequestParam String region,
        @RequestParam VersionJuego version) {

    Optional<Personaje> optPersonaje = personajeRepository.findByNombreAndReinoAndRegionAndVersionJuego(
            nombre, reino, region, version);

    if (optPersonaje.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    Personaje personaje = optPersonaje.get();

    var estadisticas = estadisticasService.obtenerEstadisticasParaVista(personaje);
    var equipo = equipoPersonajeRepository.findByPersonajeId(personaje.getId());

    Map<String, Object> json = new HashMap<>();
    json.put("nombre", personaje.getNombre());
    json.put("nivel", personaje.getNivel());
    json.put("clase", personaje.getClase());
    json.put("raza", personaje.getRaza());
    json.put("genero", personaje.getGenero());
    json.put("reino", personaje.getReino());
    json.put("region", personaje.getRegion());
    json.put("versionJuego", personaje.getVersionJuego());
    json.put("estadisticas", estadisticas);
    json.put("equipo", equipo);

    if (version == VersionJuego.retail) {
        List<TalentoDTO> talentos = talentoRetailService.obtenerTalentosParaVista(personaje);
        json.put("talentosClase", talentos.stream().filter(t -> "class".equals(t.getTipo())).toList());
        json.put("talentosSpec", talentos.stream().filter(t -> "spec".equals(t.getTipo())).toList());
        json.put("talentosHero", talentos.stream().filter(t -> "hero".equals(t.getTipo())).toList());
    } else {
        List<TalentoDTO> talentos = talentoClassicService.obtenerTalentosParaVista(personaje);
        json.put("talentos", talentos);
    }

    return ResponseEntity.ok(json);
}

}
