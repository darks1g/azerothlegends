package com.wowapp.model;

import jakarta.persistence.*;

// Entidad que representa un talento clásico en la base de datos
@Entity
@Table(name = "talento_classic")
public class TalentoClassic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único del talento

    private String nombre; // Nombre del talento
    private Integer talentoId; // Identificador del talento
    private Integer rango; // Rango del talento
    private Integer tier; // Nivel o tier del talento
    private Integer columna; // Columna en la que se encuentra el talento

    private Integer spellId;
    private String icono;


    @ManyToOne
    @JoinColumn(name = "personaje_id")
    private Personaje personaje; // Relación con la entidad Personaje

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

    public Integer getTalentoId() {
        return talentoId;
    }

    public void setTalentoId(Integer talentoId) {
        this.talentoId = talentoId;
    }

    public Integer getRango() {
        return rango;
    }

    public void setRango(Integer rango) {
        this.rango = rango;
    }

    public Integer getTier() {
        return tier;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }
    
    public Integer getSpellId() {
        return spellId;
    }
    
    public String getIcono() {
        return icono;
    }

    public void setSpellId(Integer spellId) {
        this.spellId = spellId;
    }
    
    public void setIcono(String icono) {
        this.icono = icono;
    }
    
    
}
