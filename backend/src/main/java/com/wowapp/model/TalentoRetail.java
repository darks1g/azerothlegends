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
    private Integer spellId;
    private String icono;
    private String tipo; // Puede ser: "class", "spec" o "hero"


    @ManyToOne(optional = false) // ✅ Relación obligatoria
    @JoinColumn(name = "personaje_id", nullable = false) // ✅ Clave foránea no puede ser nula
    private Personaje personaje;

    // Getters y setters

    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    

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

    public Integer getSpellId() {
        return spellId;
    }

    public void setSpellId(Integer spellId) {
        this.spellId = spellId;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }
}
