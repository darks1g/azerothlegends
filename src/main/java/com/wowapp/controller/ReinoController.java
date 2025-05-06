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

    public ReinoController(ReinoRepository reinoRepository) {
        this.reinoRepository = reinoRepository;
    }

    @GetMapping
    public List<Reino> obtenerReinosPorRegion(@RequestParam String region) {
        return reinoRepository.findByRegion(region);
    }

}
