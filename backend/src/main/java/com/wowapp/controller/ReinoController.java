package com.wowapp.controller;

import com.wowapp.model.Reino;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.ReinoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reinos")
public class ReinoController {

    private final ReinoRepository reinoRepository;

    // Constructor que inyecta el repositorio de reinos
    public ReinoController(ReinoRepository reinoRepository) {
        this.reinoRepository = reinoRepository;
    }

    // Método que maneja solicitudes GET para obtener una lista de reinos según la región
    @GetMapping
    public List<Reino> obtenerReinosPorRegion(@RequestParam String region) {
        return reinoRepository.findByRegion(region);
    }

}
