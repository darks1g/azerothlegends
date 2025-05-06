package com.wowapp.repository;

import com.wowapp.model.TalentoClassic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalentoClassicRepository extends JpaRepository<TalentoClassic, Long> {
    void deleteByPersonajeId(Long personajeId);
}