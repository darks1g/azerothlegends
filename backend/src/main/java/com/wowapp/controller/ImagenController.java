package com.wowapp.controller;

import com.wowapp.model.Personaje;
import com.wowapp.repository.PersonajeRepository;
import com.wowapp.service.ImagenService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;

// Sirve la imagen de un personaje ya recortada. Solo se descargan URLs guardadas por nosotros
// (nunca una dirección recibida del cliente), así que no se puede usar para pedir cosas arbitrarias.
@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final PersonajeRepository personajeRepository;
    private final ImagenService imagenService;

    public ImagenController(PersonajeRepository personajeRepository, ImagenService imagenService) {
        this.personajeRepository = personajeRepository;
        this.imagenService = imagenService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> imagen(@PathVariable Long id) {
        Personaje personaje = personajeRepository.findById(id).orElse(null);
        if (personaje == null || personaje.getImagenUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] png = imagenService.obtenerRecortada(personaje.getImagenUrl());
        if (png == null) {
            // Si no se pudo recortar, se redirige a la imagen original de Blizzard
            if (imagenService.urlPermitida(personaje.getImagenUrl())) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(personaje.getImagenUrl()))
                        .build();
            }
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic())
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
