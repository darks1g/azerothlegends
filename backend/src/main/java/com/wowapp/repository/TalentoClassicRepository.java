package com.wowapp.repository;

import com.wowapp.model.TalentoClassic;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio para gestionar operaciones CRUD de la entidad TalentoClassic
public interface TalentoClassicRepository extends JpaRepository<TalentoClassic, Long> {
    
    // Método para eliminar registros por el ID del personaje
    void deleteByPersonajeId(Long personajeId);
}