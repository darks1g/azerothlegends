package com.wowapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "talento_retail")
public class TalentoRetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Integer talentoId;
    private Integer rango;

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

    public Personaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }
}
