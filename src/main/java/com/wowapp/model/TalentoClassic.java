package com.wowapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "talento_classic")
public class TalentoClassic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Integer talentoId;
    private Integer rango;
    private Integer tier;
    private Integer columna;

    @ManyToOne
    @JoinColumn(name = "personaje_id")
    private Personaje personaje;

    // Getters y setters

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
}
