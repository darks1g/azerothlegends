package com.wowapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo_personaje")
public class EquipoPersonaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slot;
    private Integer itemId;
    private String nombreItem;
    private Integer ilvl;

    @ManyToOne
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getNombreItem() { return nombreItem; }
    public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

    public Integer getIlvl() { return ilvl; }
    public void setIlvl(Integer ilvl) { this.ilvl = ilvl; }

    public Personaje getPersonaje() { return personaje; }
    public void setPersonaje(Personaje personaje) { this.personaje = personaje; }
}
