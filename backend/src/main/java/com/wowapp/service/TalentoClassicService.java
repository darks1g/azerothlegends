package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.TalentoClassic;
import com.wowapp.repository.TalentoClassicRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TalentoClassicService {

    @Autowired
    private TalentoClassicRepository talentoClassicRepository;

    public void guardarTalentos(Personaje personaje, Map<String, Object> datos) {
        try {
            // Elimina los talentos existentes asociados al personaje
            talentoClassicRepository.deleteByPersonajeId(personaje.getId());

            // Obtiene los grupos de especialización del mapa de datos
            List<Map<String, Object>> grupos = (List<Map<String, Object>>) datos.get("specialization_groups");
            if (grupos == null)
                return;

            // Itera sobre los grupos de especialización
            for (Map<String, Object> grupo : grupos) {
                // Obtiene las especializaciones dentro del grupo
                List<Map<String, Object>> specs = (List<Map<String, Object>>) grupo.get("specializations");
                if (specs == null)
                    continue;

                // Itera sobre las especializaciones
                for (Map<String, Object> spec : specs) {
                    // Obtiene los talentos dentro de la especialización
                    List<Map<String, Object>> talentos = (List<Map<String, Object>>) spec.get("talents");
                    if (talentos == null)
                        continue;

                    // Itera sobre los talentos y los guarda en la base de datos
                    for (Map<String, Object> talentoData : talentos) {
                        TalentoClassic talento = new TalentoClassic();
                        talento.setPersonaje(personaje);

                        // Asigna el nivel y la columna del talento si están presentes
                        if (talentoData.containsKey("tier"))
                            talento.setTier(((Number) talentoData.get("tier")).intValue());
                        if (talentoData.containsKey("column"))
                            talento.setColumna(((Number) talentoData.get("column")).intValue());

                        // Asigna el rango del talento
                        talento.setRango(((Number) talentoData.get("talent_rank")).intValue());

                        // Obtiene información del hechizo asociado al talento
                        Map<String, Object> spellTooltip = (Map<String, Object>) talentoData.get("spell_tooltip");
                        Map<String, Object> spell = (Map<String, Object>) spellTooltip.get("spell");

                        // Asigna el ID y el nombre del talento
                        talento.setTalentoId(((Number) spell.get("id")).intValue());
                        talento.setNombre((String) spell.get("name"));

                        // Guarda el talento en la base de datos
                        talentoClassicRepository.save(talento);
                        System.out.println("Talento guardado: " + talento.getNombre());
                    }
                }
            }

            // Mensaje de confirmación al finalizar
            System.out.println("Talentos classic guardados.");

        } catch (Exception e) {
            // Manejo de errores en caso de que ocurra una excepción
            System.err.println("Error guardando talentos classic: " + e.getMessage());
        }
    }

}
