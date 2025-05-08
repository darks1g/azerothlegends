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
        // Busca si el personaje ya existe en el repositorio
        Optional<Personaje> existente = personajeRepository.findByNombreAndReinoAndRegionAndVersionJuego(
                nombre, reino, region, version);

        // Si el personaje ya existe, lo retorna
        if (existente.isPresent()) {
            return existente.get();
        }

        // Si no existe, lo obtiene desde la API
        Personaje personajeApi = apiService.obtenerPersonajeDesdeAPI(nombre, reino, region, version);
        personajeApi = personajeRepository.save(personajeApi);

        // Guarda estadísticas, talentos y equipo del personaje
        apiService.actualizarPersonaje(personajeApi);

        return personajeApi;
    }
}
