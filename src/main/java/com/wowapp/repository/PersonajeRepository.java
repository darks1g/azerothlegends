package com.wowapp.repository;

import com.wowapp.model.Personaje;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonajeRepository extends JpaRepository<Personaje, Long> {
    Optional<Personaje> findByNombreAndReinoAndRegionAndVersionJuego(
        String nombre, String reino, String region, Personaje.VersionJuego versionJuego
    );
}