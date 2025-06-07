package com.wowapp.repository;

import com.wowapp.model.EstadisticasPersonaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadisticasPersonajeRepository extends JpaRepository<EstadisticasPersonaje, Long> {
    List<EstadisticasPersonaje> findByPersonajeId(Long personajeId);
    void deleteByPersonajeId(Long personajeId); // ← ESTE ES CLAVE
}
