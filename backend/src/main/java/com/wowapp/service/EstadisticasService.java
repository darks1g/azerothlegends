package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.EstadisticasPersonaje;
import com.wowapp.repository.EstadisticasPersonajeRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.Map;
import java.util.List;
import java.util.stream.Stream;

import com.wowapp.dto.TalentoDTO;
import com.wowapp.dto.EstadisticaDTO;

@Service
public class EstadisticasService {

    private final EstadisticasPersonajeRepository EstadisticasPersonajeRepository;

    // Constructor para inyectar el repositorio de estadísticas
    public EstadisticasService(EstadisticasPersonajeRepository EstadisticasPersonajeRepository) {
        this.EstadisticasPersonajeRepository = EstadisticasPersonajeRepository;
    }

    // Método para guardar las estadísticas de un personaje en la base de datos
    public void guardarEstadisticas(Personaje personaje, Map<String, Object> datos) {
        try {
            EstadisticasPersonaje est = new EstadisticasPersonaje();
            est.setPersonaje(personaje);

            // Asignar atributos primarios desde el mapa de datos
            est.setFuerza(obtenerEnteroDesdeSubmapa(datos, "strength", "effective"));
            est.setAgilidad(obtenerEnteroDesdeSubmapa(datos, "agility", "effective"));
            est.setIntelecto(obtenerEnteroDesdeSubmapa(datos, "intellect", "effective"));
            est.setAguante(obtenerEnteroDesdeSubmapa(datos, "stamina", "effective"));

            // Asignar la vida si está presente en los datos
            if (datos.get("health") instanceof Number) {
                est.setVida(((Number) datos.get("health")).intValue());
            }

            // Asignar el golpe crítico desde el submapa "melee_crit"
            est.setGolpeCritico(obtenerDecimalDesdeSubmapa(datos, "melee_crit", "value"));

            // Asignar la celeridad desde el submapa "melee_haste"
            est.setCeleridad(obtenerDecimalDesdeSubmapa(datos, "melee_haste", "value"));

            // Asignar la maestría desde el submapa "mastery"
            est.setMaestria(obtenerDecimalDesdeSubmapa(datos, "mastery", "value"));

            // Asignar la versatilidad directamente si está presente en los datos
            if (datos.get("versatility") instanceof Number) {
                est.setVersatilidad(BigDecimal.valueOf(((Number) datos.get("versatility")).doubleValue()));
            }

            // Guardar las estadísticas en la base de datos
            EstadisticasPersonajeRepository.save(est);
            System.out.println("Estadísticas guardadas para: " + personaje.getNombre());
        } catch (Exception e) {
            // Manejo de errores al guardar las estadísticas
            System.err.println("Error al guardar estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para obtener un valor entero desde un submapa
    @SuppressWarnings("unchecked")
    private Integer obtenerEnteroDesdeSubmapa(Map<String, Object> datos, String clavePrincipal, String claveInterna) {
        try {
            Map<String, Object> submapa = (Map<String, Object>) datos.get(clavePrincipal);
            if (submapa != null && submapa.get(claveInterna) instanceof Number) {
                return ((Number) submapa.get(claveInterna)).intValue();
            }
        } catch (ClassCastException ignored) {
        }
        return 0;
    }

    // Método auxiliar para obtener un valor decimal desde un submapa
    @SuppressWarnings("unchecked")
    private BigDecimal obtenerDecimalDesdeSubmapa(Map<String, Object> datos, String clavePrincipal,
            String claveInterna) {
        try {
            Map<String, Object> submapa = (Map<String, Object>) datos.get(clavePrincipal);
            if (submapa != null && submapa.get(claveInterna) instanceof Number) {
                return BigDecimal.valueOf(((Number) submapa.get(claveInterna)).doubleValue());
            }
        } catch (ClassCastException ignored) {
        }
        return BigDecimal.ZERO;
    }

    public List<EstadisticaDTO> obtenerEstadisticasParaVista(Personaje personaje) {
        return EstadisticasPersonajeRepository.findByPersonajeId(personaje.getId())
            .stream()
            .flatMap(e -> Stream.of(
                new EstadisticaDTO("Fuerza", e.getFuerza()),
                new EstadisticaDTO("Agilidad", e.getAgilidad()),
                new EstadisticaDTO("Intelecto", e.getIntelecto()),
                new EstadisticaDTO("Aguante", e.getAguante()),
                new EstadisticaDTO("Vida", e.getVida()),
                new EstadisticaDTO("Golpe Crítico", e.getGolpeCritico().intValue()),
                new EstadisticaDTO("Celeridad", e.getCeleridad().intValue()),
                new EstadisticaDTO("Maestría", e.getMaestria().intValue()),
                new EstadisticaDTO("Versatilidad", e.getVersatilidad().intValue())
            ))
            .toList();
    }
    

}
