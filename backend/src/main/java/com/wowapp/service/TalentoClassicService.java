package com.wowapp.service;

import com.wowapp.dto.TalentoDTO;
import com.wowapp.model.Personaje;
import com.wowapp.model.TalentoClassic;
import com.wowapp.repository.TalentoClassicRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class TalentoClassicService {

    private static final Logger log = LoggerFactory.getLogger(TalentoClassicService.class);

    @Autowired
    private TalentoClassicRepository talentoClassicRepository;

    @Autowired
    private ApiService apiService;

    // El formato de Classic no está tan documentado como el de retail, así que se lee con mucha
    // tolerancia: si falta un campo se ignora ese talento en lugar de romper todo el guardado.
    @Transactional
    public void guardarTalentos(Personaje personaje, Map<String, Object> datos) {
        try {
            talentoClassicRepository.deleteByPersonajeId(personaje.getId());

            List<Map<String, Object>> grupos = lista(datos.get("specialization_groups"));
            if (grupos.isEmpty()) {
                log.warn("Sin 'specialization_groups' para {}. Respuesta: {}", personaje.getNombre(), resumen(datos));
                return;
            }

            // Classic permite doble especialización: solo se guarda el grupo activo
            Map<String, Object> activo = grupos.get(0);
            for (Map<String, Object> grupo : grupos) {
                if (Boolean.TRUE.equals(grupo.get("is_active"))) {
                    activo = grupo;
                    break;
                }
            }

            int guardados = 0;
            String arbolPrincipal = null;
            int puntosPrincipal = -1;

            for (Map<String, Object> spec : lista(activo.get("specializations"))) {
                String arbol = texto(spec.get("specialization_name"));
                int puntosArbol = 0;

                for (Map<String, Object> t : lista(spec.get("talents"))) {
                    Map<String, Object> talentInfo = mapa(t.get("talent"));
                    Map<String, Object> spell = mapa(mapa(t.get("spell_tooltip")).get("spell"));

                    Integer spellId = entero(spell.get("id"));
                    Integer talentoId = entero(talentInfo.get("id"));
                    if (spellId == null && talentoId == null) {
                        continue;
                    }

                    String nombre = texto(spell.get("name"));
                    if (nombre == null) {
                        nombre = texto(talentInfo.get("name"));
                    }
                    if (nombre == null) {
                        nombre = "Talento " + (talentoId != null ? talentoId : spellId);
                    }

                    Integer rango = entero(t.get("talent_rank"));
                    if (rango == null) {
                        rango = 1;
                    }
                    puntosArbol += rango;

                    TalentoClassic talento = new TalentoClassic();
                    talento.setPersonaje(personaje);
                    talento.setArbol(arbol);
                    talento.setNombre(nombre);
                    talento.setRango(rango);
                    talento.setSpellId(spellId);
                    talento.setTalentoId(talentoId != null ? talentoId : spellId);
                    talento.setTier(primero(entero(t.get("tier_index")), entero(t.get("tier"))));
                    talento.setColumna(primero(entero(t.get("column_index")), entero(t.get("column"))));
                    if (spellId != null) {
                        talento.setIcono(apiService.obtenerIconoDeSpell(spellId, personaje.getVersionJuego()));
                    }
                    talentoClassicRepository.save(talento);
                    guardados++;
                }

                // La especialización del personaje es el árbol con más puntos
                if (arbol != null && puntosArbol > puntosPrincipal) {
                    puntosPrincipal = puntosArbol;
                    arbolPrincipal = arbol;
                }
            }

            if (arbolPrincipal != null) {
                personaje.setEspecializacion(arbolPrincipal);
            }

            if (guardados == 0) {
                log.warn("No se pudo leer ningún talento de {}. Respuesta: {}", personaje.getNombre(), resumen(datos));
            } else {
                log.info("Talentos classic guardados para {}: {}", personaje.getNombre(), guardados);
            }
        } catch (Exception e) {
            log.error("Error guardando talentos classic: {}", e.getMessage(), e);
        }
    }

    public List<TalentoDTO> obtenerTalentosParaVista(Personaje personaje) {
        return talentoClassicRepository.findByPersonajeId(personaje.getId())
                .stream()
                .map(t -> {
                    TalentoDTO dto = new TalentoDTO(t.getNombre(), t.getSpellId() != null ? t.getSpellId() : 0, t.getIcono(), t.getArbol());
                    dto.setRango(t.getRango());
                    return dto;
                })
                .toList();
    }

    // ---- utilidades de lectura tolerante ----

    private static List<Map<String, Object>> lista(Object o) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (o instanceof List<?> l) {
            for (Object e : l) {
                if (e instanceof Map<?, ?>) {
                    resultado.add((Map<String, Object>) e);
                }
            }
        }
        return resultado;
    }

    private static Map<String, Object> mapa(Object o) {
        return o instanceof Map<?, ?> ? (Map<String, Object>) o : Map.of();
    }

    private static Integer entero(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private static String texto(Object o) {
        return o instanceof String s && !s.isBlank() ? s : null;
    }

    private static Integer primero(Integer a, Integer b) {
        return a != null ? a : b;
    }

    private static String resumen(Object o) {
        String s = String.valueOf(o);
        return s.length() > 600 ? s.substring(0, 600) + "…" : s;
    }
}
