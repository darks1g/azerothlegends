package com.wowapp.model;

import jakarta.persistence.*;
import com.wowapp.model.Personaje.VersionJuego;

@Entity
@Table(name = "reinos") // Indica que esta clase está mapeada a la tabla "reinos" en la base de datos
public class Reino {

    @Id // Marca este campo como la clave primaria de la tabla
    private Long id;

    private String nombre; // Nombre del reino

    private String slug; // Identificador único del reino en formato de texto

    private String region; // Región a la que pertenece el reino

    @Enumerated(EnumType.STRING) // Almacena el enumerado como una cadena en la base de datos
    @Column(name = "version_juego") // Mapea este campo a la columna "version_juego" en la tabla
    private VersionJuego versionJuego;

    // Métodos getter y setter para acceder y modificar los atributos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public VersionJuego getVersionJuego() {
        return versionJuego;
    }

    public void setVersionJuego(VersionJuego versionJuego) {
        this.versionJuego = versionJuego;
    }
}
