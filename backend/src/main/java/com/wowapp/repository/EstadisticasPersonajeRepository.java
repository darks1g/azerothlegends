package com.wowapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wowapp.model.EstadisticasPersonaje;
import java.util.List;
// Repositorio para manejar las operaciones CRUD de la entidad EstadisticasPersonaje
public interface EstadisticasPersonajeRepository extends JpaRepository<EstadisticasPersonaje, Long> {
    List<EstadisticasPersonaje> findByPersonajeId(Long personajeId);
}
