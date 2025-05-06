package com.wowapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personajes")
public class Personaje {

    @Id
    private Long id;

    private String nombre;
    private String reino;
    private String region;
    private Integer nivel;
    private String raza;
    private String clase;
    private String genero;

    private String especializacion;
    private Integer especializacionId;
    private String heroe;
    private Integer heroeId;


    @Enumerated(EnumType.STRING)
    @Column(name = "version_juego")
    private VersionJuego versionJuego;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters y setters

    public enum VersionJuego {
        retail,         // Shadowlands, Dragonflight...
        classic,        // Cataclysm Classic
        classic_era     // Classic Era y Hardcore
    }
    

    // Constructor vacío
    public Personaje() {}

    public void setId(Long id) {
        this.id = id;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setReino(String reino) {
        this.reino = reino;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
    
    public void setRaza(String raza) {
        this.raza = raza;
    }
    
    public void setClase(String clase) {
        this.clase = clase;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public void setVersionJuego(VersionJuego versionJuego) {
        this.versionJuego = versionJuego;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getReino() {
        return reino;
    }
    
    public String getRegion() {
        return region;
    }
    
    public Integer getNivel() {
        return nivel;
    }
    
    public String getRaza() {
        return raza;
    }
    
    public String getClase() {
        return clase;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public VersionJuego getVersionJuego() {
        return versionJuego;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public String getEspecializacion() {
        return especializacion;
    }
    
    public void setEspecializacion(String especializacion) {
        this.especializacion = especializacion;
    }
    
    public Integer getEspecializacionId() {
        return especializacionId;
    }
    
    public void setEspecializacionId(Integer especializacionId) {
        this.especializacionId = especializacionId;
    }
    
    public String getHeroe() {
        return heroe;
    }
    
    public void setHeroe(String heroe) {
        this.heroe = heroe;
    }
    
    public Integer getHeroeId() {
        return heroeId;
    }
    
    public void setHeroeId(Integer heroeId) {
        this.heroeId = heroeId;
    }
    
    
}
