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
            talentoClassicRepository.deleteByPersonajeId(personaje.getId());
    
            List<Map<String, Object>> grupos = (List<Map<String, Object>>) datos.get("specialization_groups");
            if (grupos == null) return;
    
            for (Map<String, Object> grupo : grupos) {
                List<Map<String, Object>> specs = (List<Map<String, Object>>) grupo.get("specializations");
                if (specs == null) continue;
    
                for (Map<String, Object> spec : specs) {
                    List<Map<String, Object>> talentos = (List<Map<String, Object>>) spec.get("talents");
                    if (talentos == null) continue;
    
                    for (Map<String, Object> talentoData : talentos) {
                        TalentoClassic talento = new TalentoClassic();
                        talento.setPersonaje(personaje);
    
                        // Algunos talentos pueden no tener tier/column
                        if (talentoData.containsKey("tier"))
                            talento.setTier(((Number) talentoData.get("tier")).intValue());
                        if (talentoData.containsKey("column"))
                            talento.setColumna(((Number) talentoData.get("column")).intValue());
    
                        talento.setRango(((Number) talentoData.get("talent_rank")).intValue());
    
                        Map<String, Object> spellTooltip = (Map<String, Object>) talentoData.get("spell_tooltip");
                        Map<String, Object> spell = (Map<String, Object>) spellTooltip.get("spell");
    
                        talento.setTalentoId(((Number) spell.get("id")).intValue());
                        talento.setNombre((String) spell.get("name"));
    
                        talentoClassicRepository.save(talento);
                        System.out.println("💾 Talento guardado: " + talento.getNombre());
                    }
                }
            }
    
            System.out.println("✅ Talentos classic guardados.");
    
        } catch (Exception e) {
            System.err.println("❌ Error guardando talentos classic: " + e.getMessage());
        }
    }
    
}
