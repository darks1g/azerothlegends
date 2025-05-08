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

    @Autowired
    private ApiService apiService;

    @Transactional
    public void guardarEquipo(Personaje personaje, Map<String, Object> datos) {
        try {
            // Elimina el equipo asociado al personaje antes de guardar el nuevo equipo
            equipoPersonajeRepository.deleteByPersonajeId(personaje.getId());

            // Obtiene la lista de objetos equipados del mapa de datos
            List<Map<String, Object>> items = (List<Map<String, Object>>) datos.get("equipped_items");
            if (items == null) {
                System.out.println("No se encontraron objetos equipados");
                return;
            }

            // Itera sobre cada objeto equipado y lo guarda en la base de datos
            for (Map<String, Object> itemData : items) {
                EquipoPersonaje equipo = new EquipoPersonaje();
                equipo.setPersonaje(personaje);

                // Obtiene el tipo de slot del objeto y lo asigna
                Map<String, Object> slot = (Map<String, Object>) itemData.get("slot");
                if (slot != null) {
                    equipo.setSlot((String) slot.get("type"));
                }

                // Obtiene el ID del objeto y lo asigna
                Map<String, Object> item = (Map<String, Object>) itemData.get("item");
                if (item != null && item.get("id") instanceof Number) {
                    Integer itemId = ((Number) item.get("id")).intValue();
                    equipo.setItemId(itemId);

                    // Obtener el ícono desde la API de media del ítem
                    String icono = apiService.obtenerIconoItem(itemId);
                    equipo.setIcono(icono);
                }

                // Obtiene el nombre del objeto y lo asigna
                Object nombreItem = itemData.get("name");
                if (nombreItem instanceof String) {
                    equipo.setNombreItem((String) nombreItem);
                }

                // Obtiene el nivel del objeto (ilvl) y lo asigna
                Object levelObj = itemData.get("level");
                if (levelObj instanceof Map) {
                    Map<String, Object> levelMap = (Map<String, Object>) levelObj;
                    if (levelMap.get("value") instanceof Number) {
                        equipo.setIlvl(((Number) levelMap.get("value")).intValue());
                    }
                } else if (levelObj instanceof Number) {
                    equipo.setIlvl(((Number) levelObj).intValue());
                }

                // Guarda el objeto en la base de datos
                equipoPersonajeRepository.save(equipo);
                System.out.println("Guardado equipo: " + equipo.getNombreItem() + " [" + equipo.getSlot() + "]");
            }

            // Mensaje de confirmación al finalizar el guardado
            System.out.println("Equipo guardado para: " + personaje.getNombre());

        } catch (Exception e) {
            // Manejo de errores durante el proceso de guardado
            System.err.println("Error guardando equipo: " + e.getMessage());
        }
    }
    
}
