package com.wowapp.repository;

import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.model.Reino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repositorio para la entidad Reino, que extiende JpaRepository para proporcionar métodos CRUD
public interface ReinoRepository extends JpaRepository<Reino, Long> {
    // Método personalizado para buscar reinos por región
    List<Reino> findByRegion(String region);

    // Reinos de una región y versión concretas, ordenados por nombre
    List<Reino> findByRegionAndVersionJuegoOrderByNombreAsc(String region, VersionJuego versionJuego);

    // Para saber si ya se descargaron los reinos de una región y versión
    boolean existsByRegionAndVersionJuego(String region, VersionJuego versionJuego);
}
