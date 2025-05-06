package com.wowapp.repository;

import com.wowapp.model.TalentoRetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalentoRetailRepository extends JpaRepository<TalentoRetail, Long> {
    void deleteByPersonajeId(Long personajeId);
}