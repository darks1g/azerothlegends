package com.wowapp.service;

import com.wowapp.model.Personaje;
import com.wowapp.model.PersonajeListado;
import com.wowapp.model.Usuario;
import com.wowapp.repository.PersonajeListadoRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PersonajeListadoServiceTest {

    private final PersonajeListadoRepository repository = mock(PersonajeListadoRepository.class);
    private final PersonajeListadoService service = new PersonajeListadoService(repository);

    @Test
    void guestOwnerIsStableWithinSessionAndDifferentBetweenSessions() {
        HttpSession firstSession = mock(HttpSession.class);
        HttpSession secondSession = mock(HttpSession.class);
        when(firstSession.getAttribute("personaje_guest_owner")).thenReturn(null, "guest:first");
        when(secondSession.getAttribute("personaje_guest_owner")).thenReturn(null);

        String firstOwner = service.obtenerOwnerKey(firstSession);

        assertEquals("guest:first", service.obtenerOwnerKey(firstSession));
        String secondOwner = service.obtenerOwnerKey(secondSession);
        org.junit.jupiter.api.Assertions.assertNotEquals(firstOwner, secondOwner);
        verify(firstSession).setAttribute(eq("personaje_guest_owner"), eq(firstOwner));
        verify(secondSession).setAttribute(eq("personaje_guest_owner"), eq(secondOwner));
    }

    @Test
    void registeredUserGetsAccountOwnerAndGuestSearchesAreTransferred() {
        HttpSession session = mock(HttpSession.class);
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        Personaje personaje = new Personaje();
        personaje.setId(100L);
        PersonajeListado listado = new PersonajeListado("guest:session", personaje, null);
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(session.getAttribute("personaje_guest_owner")).thenReturn("guest:session");
        when(repository.findAllByOwnerKey("guest:session")).thenReturn(List.of(listado));
        when(repository.findByOwnerKeyAndPersonajeId("user:42", 100L)).thenReturn(Optional.empty());

        assertEquals("user:42", service.obtenerOwnerKey(session));
        assertEquals("user:42", listado.getOwnerKey());
        verify(session, never()).removeAttribute("personaje_guest_owner");
    }

    @Test
    void guestSearchIsSavedUnderSessionOwner() {
        HttpSession session = mock(HttpSession.class);
        Personaje personaje = new Personaje();
        personaje.setId(100L);
        when(session.getAttribute("usuario")).thenReturn(null);
        when(session.getAttribute("personaje_guest_owner")).thenReturn("guest:session");
        when(repository.findByOwnerKeyAndPersonajeId("guest:session", 100L)).thenReturn(Optional.empty());

        service.registrarBusqueda(personaje, session);

        verify(repository).save(argThat(listado ->
                listado.getOwnerKey().equals("guest:session") && listado.getPersonaje() == personaje));
    }
}
