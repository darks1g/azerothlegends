package com.wowapp.model;

import jakarta.persistence.*;
import com.wowapp.model.Personaje.VersionJuego;

@Entity
@Table(name = "reinos")
public class Reino {

    @Id
    private Long id;

    private String nombre;

    private String slug;

    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_juego")
    private VersionJuego versionJuego;

    

    // Getters y Setters

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
