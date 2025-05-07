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

    // Constructor que inyecta el servicio de personajes
    public PersonajeController(PersonajeService personajeService) {
        this.personajeService = personajeService;
    }

    // Endpoint para crear un personaje con datos predefinidos
    @PostMapping("/crear")
    public Personaje crearPersonaje() {
        Personaje p = new Personaje();
        p.setId(81026416L); // ID desde la API oficial
        p.setNombre("Dedillos"); // Nombre del personaje
        p.setReino("Golemagg"); // Reino al que pertenece
        p.setRegion("eu"); // Región del personaje
        p.setNivel(60); // Nivel del personaje
        p.setRaza("Humano"); // Raza del personaje
        p.setClase("Mago"); // Clase del personaje
        p.setGenero("Masculino"); // Género del personaje
        p.setVersionJuego(VersionJuego.classic); // Versión del juego
        p.setFechaActualizacion(LocalDateTime.now()); // Fecha de última actualización

        // Guarda el personaje en el servicio y lo retorna
        return personajeService.guardarPersonaje(p);
    }
    
    // Endpoint para buscar un personaje basado en los datos proporcionados
    @PostMapping("/buscar")
    public ResponseEntity<Personaje> buscarPersonaje(@RequestBody Map<String, String> datos) {
        String nombre = datos.get("nombre"); // Obtiene el nombre del personaje
        String reino = datos.get("reino"); // Obtiene el reino del personaje
        String region = datos.get("region"); // Obtiene la región del personaje
        String versionStr = datos.get("version").toLowerCase(); // Obtiene la versión del juego en minúsculas

        // Convierte la versión del juego a un valor del enum VersionJuego
        VersionJuego version = VersionJuego.valueOf(versionStr);

        // Obtiene el personaje desde el servicio y lo guarda
        Personaje personaje = personajeService.obtenerYGuardarPersonaje(nombre, reino, region, version);
        return ResponseEntity.ok(personaje); // Retorna el personaje en la respuesta
    }

}
