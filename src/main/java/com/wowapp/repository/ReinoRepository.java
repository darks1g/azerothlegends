package com.wowapp.repository;

import com.wowapp.model.Reino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReinoRepository extends JpaRepository<Reino, Long> {
    List<Reino> findByRegion(String region);
}
