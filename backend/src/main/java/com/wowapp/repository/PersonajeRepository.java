package com.wowapp.repository;

import com.wowapp.model.Personaje;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Repositorio para la entidad Personaje (JpaSpecificationExecutor permite filtrar en el listado)
public interface PersonajeRepository extends JpaRepository<Personaje, Long>, JpaSpecificationExecutor<Personaje> {

    // Método para buscar un personaje por nombre, reino, región y versión del juego
    Optional<Personaje> findByNombreAndReinoAndRegionAndVersionJuego(
        String nombre, String reino, String region, Personaje.VersionJuego versionJuego
    );

    // Igual que el anterior pero sin distinguir mayúsculas en el nombre ni en el reino
    Optional<Personaje> findByNombreIgnoreCaseAndReinoIgnoreCaseAndRegionAndVersionJuego(
        String nombre, String reino, String region, Personaje.VersionJuego versionJuego
    );
}
