package com.wowapp.repository;

import com.wowapp.model.Personaje;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio para la entidad Personaje, que extiende JpaRepository
public interface PersonajeRepository extends JpaRepository<Personaje, Long> {
    
    // Método para buscar un personaje por nombre, reino, región y versión del juego
    Optional<Personaje> findByNombreAndReinoAndRegionAndVersionJuego(
        String nombre, String reino, String region, Personaje.VersionJuego versionJuego
    );
}