package com.wowapp.repository;

import com.wowapp.model.EquipoPersonaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoPersonajeRepository extends JpaRepository<EquipoPersonaje, Long> {
    void deleteByPersonajeId(Long personajeId);
}
