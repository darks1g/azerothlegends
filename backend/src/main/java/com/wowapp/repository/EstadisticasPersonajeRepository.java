package com.wowapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wowapp.model.EstadisticasPersonaje;

// Repositorio para manejar las operaciones CRUD de la entidad EstadisticasPersonaje
public interface EstadisticasPersonajeRepository extends JpaRepository<EstadisticasPersonaje, Long> {
}
