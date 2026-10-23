package com.wowapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ApiServiceTest {

    @Test
    void seleccionarImagenPriorizaLaImagenPrincipalAunqueLlegueDespuesDelAvatar() {
        List<Map<String, String>> assets = List.of(
                Map.of("key", "avatar", "value", "avatar.jpg"),
                Map.of("key", "inset", "value", "inset.jpg"),
                Map.of("key", "main", "value", "main.jpg"));

        assertEquals("main.jpg", ApiService.seleccionarImagen(assets));
    }

    @Test
    void seleccionarImagenPrefiereMainRawYUsaElAvatarComoUltimoRecurso() {
        List<Map<String, String>> assets = List.of(
                Map.of("key", "main", "value", "main.jpg"),
                Map.of("key", "main-raw", "value", "main-raw.jpg"));
        assertEquals("main-raw.jpg", ApiService.seleccionarImagen(assets));

        assertEquals("avatar.jpg", ApiService.seleccionarImagen(
                List.of(Map.of("key", "avatar", "value", "avatar.jpg"))));
    }

    @Test
    void convertirPersonalizacionesUsaLosIdsDeOpcionYEleccionDeBlizzard() {
        List<Map<String, Object>> personalizaciones = List.of(
                Map.of("option", Map.of("id", 97), "choice", Map.of("id", 17878)),
                Map.of("option", Map.of("id", 98), "choice", Map.of("id", 17885)));

        assertEquals(List.of(
                Map.of("optionId", 97, "choiceId", 17878),
                Map.of("optionId", 98, "choiceId", 17885)),
                ApiService.convertirPersonalizaciones(personalizaciones));
    }
}
