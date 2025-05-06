package com.wowapp.controller;

import com.wowapp.model.BuscarPersonajeRequest;
import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.service.PersonajeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/personajes")
public class PersonajeController {

    private final PersonajeService personajeService;

    public PersonajeController(PersonajeService personajeService) {
        this.personajeService = personajeService;
    }

    @PostMapping("/crear")
    public Personaje crearPersonaje() {
        Personaje p = new Personaje();
        p.setId(81026416L); // ID desde la API oficial
        p.setNombre("Dedillos");
        p.setReino("Golemagg");
        p.setRegion("eu");
        p.setNivel(60);
        p.setRaza("Humano");
        p.setClase("Mago");
        p.setGenero("Masculino");
        p.setVersionJuego(VersionJuego.classic);
        p.setFechaActualizacion(LocalDateTime.now());

        return personajeService.guardarPersonaje(p);
    }
    
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

}
