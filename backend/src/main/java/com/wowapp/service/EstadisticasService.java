package com.wowapp.service;

import com.wowapp.model.EstadisticasPersonaje;
import com.wowapp.model.Personaje;
import com.wowapp.dto.EstadisticaDTO;
import com.wowapp.repository.EstadisticasPersonajeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class EstadisticasService {

    @Autowired
    private EstadisticasPersonajeRepository estadisticasPersonajeRepository;

    public void guardarEstadisticas(Personaje personaje, Map<String, Object> datos) {
        estadisticasPersonajeRepository.deleteByPersonajeId(personaje.getId());

        EstadisticasPersonaje e = new EstadisticasPersonaje();
        e.setPersonaje(personaje);
        e.setFuerza(toInteger(datos.get("strength")));
        e.setAgilidad(toInteger(datos.get("agility")));
        e.setIntelecto(toInteger(datos.get("intellect")));
        e.setAguante(toInteger(datos.get("stamina")));
        e.setVida(toInteger(datos.get("health")));
        e.setGolpeCritico(toBigDecimal(datos.get("crit")));
        e.setCeleridad(toBigDecimal(datos.get("haste")));
        e.setMaestria(toBigDecimal(datos.get("mastery")));
        e.setVersatilidad(toBigDecimal(datos.get("versatility")));

        estadisticasPersonajeRepository.save(e);
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private Integer toInteger(Object valor) {
        if (valor instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    public List<EstadisticaDTO> obtenerEstadisticasParaVista(Personaje personaje) {
    return estadisticasPersonajeRepository.findByPersonajeId(personaje.getId())
        .stream()
        .flatMap(e -> Stream.of(
            new EstadisticaDTO("Fuerza", e.getFuerza().intValue()),
            new EstadisticaDTO("Agilidad", e.getAgilidad().intValue()),
            new EstadisticaDTO("Intelecto", e.getIntelecto().intValue()),
            new EstadisticaDTO("Aguante", e.getAguante().intValue()),
            new EstadisticaDTO("Vida", e.getVida().intValue()),
            new EstadisticaDTO("Golpe Crítico", e.getGolpeCritico().intValue()),
            new EstadisticaDTO("Celeridad", e.getCeleridad().intValue()),
            new EstadisticaDTO("Maestría", e.getMaestria().intValue()),
            new EstadisticaDTO("Versatilidad", e.getVersatilidad().intValue())
        ))
        .toList();
}
}
