package com.wowapp.model;

import jakarta.persistence.*;

// Clase que representa la entidad "TalentoRetail" mapeada a la tabla "talento_retail" en la base de datos
@Entity
@Table(name = "talento_retail")
public class TalentoRetail {

    // Identificador único de la entidad, generado automáticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del talento
    private String nombre;

    // Identificador del talento
    private Integer talentoId;

    // Rango del talento
    private Integer rango;

    // Relación muchos-a-uno con la entidad "Personaje"
    @ManyToOne
    @JoinColumn(name = "personaje_id") // Llave foránea que referencia a "personaje_id" en la tabla relacionada
    private Personaje personaje;

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

    public Personaje getPersonaje() {
        return personaje;
    }

    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }
}
