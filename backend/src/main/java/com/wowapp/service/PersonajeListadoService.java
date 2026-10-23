package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.PersonajeListado;
import com.wowapp.model.Usuario;
import com.wowapp.repository.PersonajeListadoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PersonajeListadoService {

    private static final String GUEST_OWNER_SESSION_KEY = "personaje_guest_owner";

    private final PersonajeListadoRepository listadoRepository;

    public PersonajeListadoService(PersonajeListadoRepository listadoRepository) {
        this.listadoRepository = listadoRepository;
    }

    @Transactional
    public String obtenerOwnerKey(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return obtenerOwnerInvitado(session);
        }

        String ownerKey = "user:" + usuario.getId();
        String guestOwnerKey = (String) session.getAttribute(GUEST_OWNER_SESSION_KEY);
        if (guestOwnerKey != null) {
            listadoRepository.findAllByOwnerKey(guestOwnerKey).forEach(listado -> {
                var existente = listadoRepository.findByOwnerKeyAndPersonajeId(ownerKey, listado.getPersonaje().getId());
                if (existente.isPresent()) {
                    listadoRepository.delete(listado);
                } else {
                    listado.setOwnerKey(ownerKey);
                }
            });
        }
        return ownerKey;
    }

    @Transactional
    public void registrarBusqueda(Personaje personaje, HttpSession session) {
        String ownerKey = obtenerOwnerKey(session);
        LocalDateTime ahora = LocalDateTime.now();
        PersonajeListado listado = listadoRepository
                .findByOwnerKeyAndPersonajeId(ownerKey, personaje.getId())
                .orElseGet(() -> new PersonajeListado(ownerKey, personaje, ahora));
        listado.setUltimaBusqueda(ahora);
        listadoRepository.save(listado);
    }

    private String obtenerOwnerInvitado(HttpSession session) {
        String ownerKey = (String) session.getAttribute(GUEST_OWNER_SESSION_KEY);
        if (ownerKey == null) {
            ownerKey = "guest:" + UUID.randomUUID();
            session.setAttribute(GUEST_OWNER_SESSION_KEY, ownerKey);
        }
        return ownerKey;
    }
}
