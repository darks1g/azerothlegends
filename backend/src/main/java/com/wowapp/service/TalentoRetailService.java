package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.TalentoRetail;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.repository.TalentoRetailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TalentoRetailService {

    @Autowired
    private TalentoRetailRepository talentoRetailRepository;

    @Autowired
    private PersonajeRepository personajeRepository;

    @Transactional
    public void guardarTalentos(Personaje personaje, Map<String, Object> datos) {
        try {
            // Eliminar talentos existentes asociados al personaje
            talentoRetailRepository.deleteByPersonajeId(personaje.getId());

            // Obtener la lista de especializaciones del mapa de datos
            List<Map<String, Object>> specializations = (List<Map<String, Object>>) datos.get("specializations");
            if (specializations == null) {
                System.out.println("No hay campo 'specializations'");
                return;
            }

            // Iterar sobre las especializaciones
            for (Map<String, Object> spec : specializations) {
                // Obtener las configuraciones de talentos de la especialización
                List<Map<String, Object>> loadouts = (List<Map<String, Object>>) spec.get("loadouts");
                if (loadouts == null)
                    continue;

                // Iterar sobre las configuraciones de talentos
                for (Map<String, Object> loadout : loadouts) {
                    // Obtener los talentos seleccionados
                    List<Map<String, Object>> talentos = (List<Map<String, Object>>) loadout
                            .get("selected_class_talents");
                    if (talentos == null)
                        continue;

                    // Iterar sobre los talentos y guardarlos
                    for (Map<String, Object> talentoData : talentos) {
                        TalentoRetail talento = new TalentoRetail();
                        talento.setPersonaje(personaje);

                        // Obtener información del talento desde el tooltip
                        Map<String, Object> tooltip = (Map<String, Object>) talentoData.get("tooltip");
                        if (tooltip == null)
                            continue;

                        Map<String, Object> talent = (Map<String, Object>) tooltip.get("talent");
                        if (talent == null)
                            continue;

                        // Configurar los datos del talento
                        talento.setTalentoId(((Number) talent.get("id")).intValue());
                        talento.setNombre((String) talent.get("name"));
                        talento.setRango(((Number) talentoData.get("rank")).intValue());

                        // Guardar el talento en la base de datos
                        talentoRetailRepository.save(talento);
                        System.out.println("Talento retail guardado: " + talento.getNombre());
                    }
                }
            }

            // Guardar la especialización activa del personaje
            Map<String, Object> activeSpec = (Map<String, Object>) datos.get("active_specialization");
            if (activeSpec != null) {
                personaje.setEspecializacion((String) activeSpec.get("name"));
                personaje.setEspecializacionId(((Number) activeSpec.get("id")).intValue());
            }

            // Guardar el talento de héroe activo del personaje
            Map<String, Object> heroTree = (Map<String, Object>) datos.get("active_hero_talent_tree");
            if (heroTree != null) {
                personaje.setHeroe((String) heroTree.get("name"));
                personaje.setHeroeId(((Number) heroTree.get("id")).intValue());
            }

            // Guardar el personaje actualizado en la base de datos
            personajeRepository.save(personaje);

            System.out.println("Talentos retail guardados para: " + personaje.getNombre());

        } catch (Exception e) {
            // Manejo de errores durante el proceso de guardado
            System.err.println("Error guardando talentos retail: " + e.getMessage());
        }
    }
}
