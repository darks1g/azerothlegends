package com.wowapp.repository;

import com.wowapp.model.Reino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repositorio para la entidad Reino, que extiende JpaRepository para proporcionar métodos CRUD
public interface ReinoRepository extends JpaRepository<Reino, Long> {
    // Método personalizado para buscar reinos por región
    List<Reino> findByRegion(String region);
}
