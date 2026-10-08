package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.PersonajeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonajeService {

    private static final Logger log = LoggerFactory.getLogger(PersonajeService.class);

    private final PersonajeRepository personajeRepository;
    private final ApiService apiService;

    public PersonajeService(PersonajeRepository personajeRepository, ApiService apiService) {
        this.personajeRepository = personajeRepository;
        this.apiService = apiService;
    }

    public Personaje guardarPersonaje(Personaje personaje) {
        return personajeRepository.save(personaje);
    }

    // Devuelve el personaje de la base de datos (refrescándolo si hace falta) o lo descarga de Blizzard
    public Personaje obtenerYGuardarPersonaje(String nombre, String reino, String region, VersionJuego version) {
        Optional<Personaje> existente = personajeRepository
                .findByNombreIgnoreCaseAndReinoIgnoreCaseAndRegionAndVersionJuego(nombre.trim(), reino.trim(), region,
                        version);

        if (existente.isPresent()) {
            Personaje personaje = existente.get();
            log.info("Personaje encontrado en la base de datos: {}", personaje.getNombre());
            apiService.actualizarPersonaje(personaje); // actualiza si hace falta
            return personaje;
        }

        log.info("Personaje no encontrado en la base de datos, consultando a Blizzard...");
        Personaje personajeApi = apiService.obtenerPersonajeDesdeAPI(nombre, reino, region, version);
        personajeApi = personajeRepository.save(personajeApi);
        apiService.actualizarPersonaje(personajeApi);
        return personajeApi;
    }
}
