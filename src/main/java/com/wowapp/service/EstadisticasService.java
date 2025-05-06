package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.EstadisticasPersonaje;
import com.wowapp.repository.EstadisticasPersonajeRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.Map;

@Service
public class EstadisticasService {

    private final EstadisticasPersonajeRepository EstadisticasPersonajeRepository;

    public EstadisticasService(EstadisticasPersonajeRepository EstadisticasPersonajeRepository) {
        this.EstadisticasPersonajeRepository = EstadisticasPersonajeRepository;
    }

    public void guardarEstadisticas(Personaje personaje, Map<String, Object> datos) {
        try {
            EstadisticasPersonaje est = new EstadisticasPersonaje();
            est.setPersonaje(personaje);
    
            // Atributos primarios
            est.setFuerza(obtenerEnteroDesdeSubmapa(datos, "strength", "effective"));
            est.setAgilidad(obtenerEnteroDesdeSubmapa(datos, "agility", "effective"));
            est.setIntelecto(obtenerEnteroDesdeSubmapa(datos, "intellect", "effective"));
            est.setAguante(obtenerEnteroDesdeSubmapa(datos, "stamina", "effective"));
    
            // Vida
            if (datos.get("health") instanceof Number) {
                est.setVida(((Number) datos.get("health")).intValue());
            }
    
            // Golpe crítico desde "melee_crit"
            est.setGolpeCritico(obtenerDecimalDesdeSubmapa(datos, "melee_crit", "value"));
    
            // Celeridad desde "melee_haste"
            est.setCeleridad(obtenerDecimalDesdeSubmapa(datos, "melee_haste", "value"));
    
            // Maestría funciona bien
            est.setMaestria(obtenerDecimalDesdeSubmapa(datos, "mastery", "value"));
    
            // Versatilidad es un número directo
            if (datos.get("versatility") instanceof Number) {
                est.setVersatilidad(BigDecimal.valueOf(((Number) datos.get("versatility")).doubleValue()));
            }
    
            EstadisticasPersonajeRepository.save(est);
            System.out.println("Estadísticas guardadas para: " + personaje.getNombre());
        } catch (Exception e) {
            System.err.println("Error al guardar estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    

    @SuppressWarnings("unchecked")
private Integer obtenerEnteroDesdeSubmapa(Map<String, Object> datos, String clavePrincipal, String claveInterna) {
    try {
        Map<String, Object> submapa = (Map<String, Object>) datos.get(clavePrincipal);
        if (submapa != null && submapa.get(claveInterna) instanceof Number) {
            return ((Number) submapa.get(claveInterna)).intValue();
        }
    } catch (ClassCastException ignored) {}
    return 0;
}

@SuppressWarnings("unchecked")
private BigDecimal obtenerDecimalDesdeSubmapa(Map<String, Object> datos, String clavePrincipal, String claveInterna) {
    try {
        Map<String, Object> submapa = (Map<String, Object>) datos.get(clavePrincipal);
        if (submapa != null && submapa.get(claveInterna) instanceof Number) {
            return BigDecimal.valueOf(((Number) submapa.get(claveInterna)).doubleValue());
        }
    } catch (ClassCastException ignored) {}
    return BigDecimal.ZERO;
}

    
}


