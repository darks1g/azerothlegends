package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.TalentoRetail;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.repository.TalentoRetailRepository;
import com.wowapp.dto.TalentoDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TalentoRetailService {

    @Autowired
    private TalentoRetailRepository talentoRetailRepository;

    @Autowired
    private PersonajeRepository personajeRepository;

    @Autowired
    private ApiService apiService;

    @Transactional
    public void guardarTalentos(Personaje personaje, Map<String, Object> datos) {
        try {
            talentoRetailRepository.deleteByPersonajeId(personaje.getId());

            List<Map<String, Object>> specializations = (List<Map<String, Object>>) datos.get("specializations");
            if (specializations == null || specializations.isEmpty()) {
                System.out.println("⚠️ No hay campo 'specializations' o está vacío.");
                return;
            }

            // Solo se guardan los talentos de la especialización activa (la API devuelve todas)
            Integer idActiva = null;
            if (datos.get("active_specialization") instanceof Map<?, ?> activa && activa.get("id") instanceof Number n) {
                idActiva = n.intValue();
            }

            for (Map<String, Object> spec : specializations) {
                if (idActiva != null && spec.get("specialization") instanceof Map<?, ?> info
                        && info.get("id") instanceof Number idSpec && idSpec.intValue() != idActiva) {
                    continue;
                }
                List<Map<String, Object>> loadouts = (List<Map<String, Object>>) spec.get("loadouts");
                if (loadouts == null)
                    continue;

                for (Map<String, Object> loadout : loadouts) {
                    if (!Boolean.TRUE.equals(loadout.get("is_active")))
                        continue;

                    // 🟡 Class talents
                    List<Map<String, Object>> classTalents = (List<Map<String, Object>>) loadout
                            .get("selected_class_talents");
                    guardarListaDeTalentos(classTalents, personaje, "class");

                    // 🔵 Spec talents
                    List<Map<String, Object>> specTalents = (List<Map<String, Object>>) loadout
                            .get("selected_spec_talents");
                    guardarListaDeTalentos(specTalents, personaje, "spec");

                    // 🟣 Hero talents
                    List<Map<String, Object>> heroTalents = (List<Map<String, Object>>) loadout
                            .get("selected_hero_talents");
                    guardarListaDeTalentos(heroTalents, personaje, "hero");
                }
            }

            // Especialización activa
            Map<String, Object> activeSpec = (Map<String, Object>) datos.get("active_specialization");
            if (activeSpec != null) {
                personaje.setEspecializacion((String) activeSpec.get("name"));
                personaje.setEspecializacionId(((Number) activeSpec.get("id")).intValue());
            }

            // Hero talent tree activo
            Map<String, Object> heroTree = (Map<String, Object>) datos.get("active_hero_talent_tree");
            if (heroTree != null) {
                personaje.setHeroe((String) heroTree.get("name"));
                personaje.setHeroeId(((Number) heroTree.get("id")).intValue());
            }

            personajeRepository.save(personaje);
            System.out.println("✔️ Talentos retail guardados para: " + personaje.getNombre());

        } catch (Exception e) {
            System.err.println("❌ Error guardando talentos retail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void guardarListaDeTalentos(List<Map<String, Object>> talentos, Personaje personaje, String tipo) {
        if (talentos == null || talentos.isEmpty())
            return;

        for (Map<String, Object> talentoData : talentos) {
            try {
                TalentoRetail talento = new TalentoRetail();
                talento.setPersonaje(personaje);
                talento.setTalentoId(((Number) talentoData.get("id")).intValue());
                talento.setRango(((Number) talentoData.get("rank")).intValue());
                talento.setTipo(tipo);

                Map<String, Object> tooltip = (Map<String, Object>) talentoData.get("tooltip");
                if (tooltip != null) {
                    Map<String, Object> talentInfo = (Map<String, Object>) tooltip.get("talent");
                    Map<String, Object> spell = (Map<String, Object>) tooltip.get("spell_tooltip") != null
                            ? (Map<String, Object>) ((Map<String, Object>) tooltip.get("spell_tooltip")).get("spell")
                            : null;

                    if (talentInfo != null && talentInfo.get("name") != null) {
                        talento.setNombre((String) talentInfo.get("name"));
                    }

                    if (spell != null && spell.get("id") != null) {
                        int spellId = ((Number) spell.get("id")).intValue();
                        talento.setSpellId(spellId);
                        talento.setIcono(apiService.obtenerIconoDeSpell(spellId, personaje.getVersionJuego()));
                    }
                }

                if (talento.getNombre() == null) {
                    talento.setNombre("Talento " + tipo + " ID " + talento.getTalentoId());
                }
                if (talento.getSpellId() == null) {
                    talento.setSpellId(0);
                    talento.setIcono("inv_misc_questionmark");
                }

                talentoRetailRepository.save(talento);

            } catch (Exception e) {
                System.err.println("❌ Error guardando talento [" + tipo + "]: " + e.getMessage());
            }
        }
    }

    public List<TalentoDTO> obtenerTalentosParaVista(Personaje personaje) {
        return talentoRetailRepository.findByPersonajeId(personaje.getId())
            .stream()
            .map(t -> new TalentoDTO(
                t.getNombre(),
                t.getSpellId() != null ? t.getSpellId() : 0,
                t.getIcono() != null ? t.getIcono() : "inv_misc_questionmark",
                t.getTipo()
            ))
            .toList();
    }
}
