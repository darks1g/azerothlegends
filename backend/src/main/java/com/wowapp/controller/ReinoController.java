package com.wowapp.controller;

import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.model.Reino;
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

    // Lista los reinos de una región; si se indica la versión del juego, solo los de esa versión
    @GetMapping
    public List<Reino> obtenerReinosPorRegion(@RequestParam String region,
            @RequestParam(required = false) VersionJuego version) {
        if (version != null) {
            return reinoRepository.findByRegionAndVersionJuegoOrderByNombreAsc(region, version);
        }
        return reinoRepository.findByRegion(region);
    }
}
