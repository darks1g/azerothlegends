package com.wowapp.repository;

import com.wowapp.model.TalentoRetail;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio para gestionar la entidad TalentoRetail en la base de datos
public interface TalentoRetailRepository extends JpaRepository<TalentoRetail, Long> {

    // Método para eliminar registros por el ID del personaje
    void deleteByPersonajeId(Long personajeId);
}