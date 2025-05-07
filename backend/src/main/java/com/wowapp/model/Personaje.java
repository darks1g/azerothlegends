package com.wowapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personajes") // Define que esta clase es una entidad y se mapea a la tabla "personajes"
public class Personaje {

    @ManyToOne // Relación muchos a uno con la entidad Usuario
    @JoinColumn(name = "usuario_id") // Define la columna "usuario_id" como clave foránea
    private Usuario usuario;

    @Id // Define el campo "id" como clave primaria
    private Long id;

    // Atributos de la entidad
    private String nombre; // Nombre del personaje
    private String reino; // Reino al que pertenece el personaje
    private String region; // Región del personaje
    private Integer nivel; // Nivel del personaje
    private String raza; // Raza del personaje
    private String clase; // Clase del personaje
    private String genero; // Género del personaje

    private String especializacion; // Especialización del personaje
    private Integer especializacionId; // ID de la especialización
    private String heroe; // Nombre del héroe asociado
    private Integer heroeId; // ID del héroe asociado

    @Enumerated(EnumType.STRING) // Define que el enum se almacenará como texto en la base de datos
    @Column(name = "version_juego") // Nombre de la columna en la tabla
    private VersionJuego versionJuego; // Versión del juego asociada al personaje

    @Column(name = "fecha_actualizacion") // Nombre de la columna en la tabla
    private LocalDateTime fechaActualizacion; // Fecha de la última actualización del personaje

    // Enumeración para las versiones del juego
    public enum VersionJuego {
        retail,         // Shadowlands, Dragonflight...
        classic,        // Cataclysm Classic
        classic_era     // Classic Era y Hardcore
    }

    // Constructor vacío necesario para JPA
    public Personaje() {}

    // Métodos setters para asignar valores a los atributos
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

    // Métodos getters para obtener los valores de los atributos
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
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
