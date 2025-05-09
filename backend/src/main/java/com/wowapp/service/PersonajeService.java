package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.PersonajeRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;
    private final ApiService apiService;

    // Constructor para inyectar dependencias del repositorio y servicio de API
    public PersonajeService(PersonajeRepository personajeRepository, ApiService apiService) {
        this.personajeRepository = personajeRepository;
        this.apiService = apiService;
    }

    // Guarda un personaje en el repositorio
    public Personaje guardarPersonaje(Personaje personaje) {
        return personajeRepository.save(personaje);
    }

    // Obtiene un personaje desde el repositorio o la API y lo guarda
    public Personaje obtenerYGuardarPersonaje(String nombre, String reino, String region, VersionJuego version) {
        Optional<Personaje> existente = personajeRepository.findByNombreAndReinoAndRegionAndVersionJuego(
                nombre, reino, region, version);

        if (existente.isPresent()) {
            Personaje personaje = existente.get();
            System.out.println("🔍 Personaje encontrado en la base de datos: " + personaje.getNombre());

            apiService.actualizarPersonaje(personaje); // ← actualiza si hace falta
            return personaje;
        }

        // Si no existe, lo obtiene desde la API
        System.out.println("🆕 Personaje no encontrado, consultando a la API...");
        Personaje personajeApi = apiService.obtenerPersonajeDesdeAPI(nombre, reino, region, version);
        personajeApi = personajeRepository.save(personajeApi);

        apiService.actualizarPersonaje(personajeApi);
        return personajeApi;
    }
}
