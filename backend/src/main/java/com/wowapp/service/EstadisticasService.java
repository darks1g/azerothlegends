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
        // Blizzard manda los atributos como {base, effective} y los porcentajes como {rating, value}
        e.setFuerza(toInteger(sub(datos.get("strength"), "effective")));
        e.setAgilidad(toInteger(sub(datos.get("agility"), "effective")));
        e.setIntelecto(toInteger(sub(datos.get("intellect"), "effective")));
        e.setAguante(toInteger(sub(datos.get("stamina"), "effective")));
        e.setVida(toInteger(datos.get("health")));
        e.setGolpeCritico(toBigDecimal(maximo(datos, "melee_crit", "spell_crit", "ranged_crit")));
        e.setCeleridad(toBigDecimal(maximo(datos, "melee_haste", "spell_haste", "ranged_haste")));
        e.setMaestria(toBigDecimal(sub(datos.get("mastery"), "value")));
        // "versatility" es la puntuación (306); el porcentaje (6 %) está en este otro campo
        e.setVersatilidad(toBigDecimal(datos.get("versatility_damage_done_bonus")));

        estadisticasPersonajeRepository.save(e);
    }

    // Si el valor es un objeto devuelve su campo; si es un número suelto, lo devuelve tal cual
    private Object sub(Object valor, String clave) {
        return valor instanceof Map<?, ?> m ? m.get(clave) : valor;
    }

    // De varias estadísticas equivalentes (cuerpo a cuerpo, hechizo, distancia) toma la más alta
    private Object maximo(Map<String, Object> datos, String... claves) {
        double mejor = -1;
        for (String clave : claves) {
            Object v = sub(datos.get(clave), "value");
            if (v instanceof Number n && n.doubleValue() > mejor) {
                mejor = n.doubleValue();
            }
        }
        return mejor < 0 ? null : mejor;
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
            new EstadisticaDTO("Golpe Crítico", e.getGolpeCritico().setScale(0, java.math.RoundingMode.HALF_UP).intValue()),
            new EstadisticaDTO("Celeridad", e.getCeleridad().setScale(0, java.math.RoundingMode.HALF_UP).intValue()),
            new EstadisticaDTO("Maestría", e.getMaestria().setScale(0, java.math.RoundingMode.HALF_UP).intValue()),
            new EstadisticaDTO("Versatilidad", e.getVersatilidad().setScale(0, java.math.RoundingMode.HALF_UP).intValue())
        ))
        .toList();
}
}
