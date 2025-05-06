package com.wowapp.service;

import com.wowapp.model.EquipoPersonaje;
import com.wowapp.model.Personaje;
import com.wowapp.repository.EquipoPersonajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EquipoPersonajeService {

    @Autowired
    private EquipoPersonajeRepository equipoPersonajeRepository;

    @Transactional
    public void guardarEquipo(Personaje personaje, Map<String, Object> datos) {
        try {
            equipoPersonajeRepository.deleteByPersonajeId(personaje.getId());

            List<Map<String, Object>> items = (List<Map<String, Object>>) datos.get("equipped_items");
            if (items == null) {
                System.out.println("🚫 No se encontraron objetos equipados");
                return;
            }

            for (Map<String, Object> itemData : items) {
                EquipoPersonaje equipo = new EquipoPersonaje();
                equipo.setPersonaje(personaje);

                // SLOT
                Map<String, Object> slot = (Map<String, Object>) itemData.get("slot");
                if (slot != null) {
                    equipo.setSlot((String) slot.get("type"));
                }

                // ITEM ID
                Map<String, Object> item = (Map<String, Object>) itemData.get("item");
                if (item != null && item.get("id") instanceof Number) {
                    equipo.setItemId(((Number) item.get("id")).intValue());
                }

                // NOMBRE
                Object nombreItem = itemData.get("name");
                if (nombreItem instanceof String) {
                    equipo.setNombreItem((String) nombreItem);
                }


                // ILVL
                Object levelObj = itemData.get("level");
                if (levelObj instanceof Map) {
                    Map<String, Object> levelMap = (Map<String, Object>) levelObj;
                    if (levelMap.get("value") instanceof Number) {
                        equipo.setIlvl(((Number) levelMap.get("value")).intValue());
                    }
                } else if (levelObj instanceof Number) {
                    equipo.setIlvl(((Number) levelObj).intValue());
                }


                equipoPersonajeRepository.save(equipo);
                System.out.println("💾 Guardado equipo: " + equipo.getNombreItem() + " [" + equipo.getSlot() + "]");
            }

            System.out.println("✅ Equipo guardado para: " + personaje.getNombre());

        } catch (Exception e) {
            System.err.println("❌ Error guardando equipo: " + e.getMessage());
        }
    }
}
