package com.wowapp.controller;

import com.wowapp.model.Personaje;
import com.wowapp.model.EquipoPersonaje;
import com.wowapp.model.Personaje.VersionJuego;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.repository.EquipoPersonajeRepository;
import com.wowapp.service.EstadisticasService;
import com.wowapp.service.TalentoRetailService;
import com.wowapp.service.TalentoClassicService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Controller
public class DetallesPersonajeController {

    @Autowired
    private PersonajeRepository personajeRepository;

    @Autowired
    private EquipoPersonajeRepository equipoPersonajeRepository;

    @Autowired
    private EstadisticasService estadisticasService;

    @Autowired
    private TalentoRetailService talentoRetailService;

    @Autowired
    private TalentoClassicService talentoClassicService;

    @GetMapping("/personaje/{nombre}")
    public String mostrarDetallesPersonaje(
            @PathVariable String nombre,
            @RequestParam String reino,
            @RequestParam String region,
            @RequestParam VersionJuego version,
            Model model) {

        Personaje personaje = personajeRepository.findByNombreAndReinoAndRegionAndVersionJuego(
                nombre, reino, region, version).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado."));

        List<EquipoPersonaje> equipo = equipoPersonajeRepository.findByPersonajeId(personaje.getId());
        var estadisticas = estadisticasService.obtenerEstadisticasParaVista(personaje);
        var talentos = switch (version) {
            case retail -> talentoRetailService.obtenerTalentosParaVista(personaje);
            case classic, classic_era -> talentoClassicService.obtenerTalentosParaVista(personaje);
        };

        model.addAttribute("personaje", personaje);
        model.addAttribute("equipo", equipo);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("talentos", talentos);

        return "detalles";
    }
}
