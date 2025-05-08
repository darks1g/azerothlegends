package com.wowapp.model;

import jakarta.persistence.*;

// Entidad que representa el equipo de un personaje en la base de datos
@Entity
@Table(name = "equipo_personaje")
public class EquipoPersonaje {

    // Identificador único de la entidad, generado automáticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Slot donde se equipa el ítem (por ejemplo: cabeza, pecho, etc.)
    private String slot;

    // Identificador del ítem (puede ser un ID único del ítem en el sistema)
    private Integer itemId;

    // Nombre del ítem equipado
    private String nombreItem;

    // Nivel de ítem (ilvl) que indica la calidad o poder del ítem
    private Integer ilvl;

    // Relación muchos-a-uno con la entidad Personaje
    // Indica a qué personaje pertenece este equipo
    @ManyToOne
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    // Getters y Setters

    // Obtiene el ID del equipo
    public Long getId() { return id; }
    // Establece el ID del equipo
    public void setId(Long id) { this.id = id; }

    // Obtiene el slot del equipo
    public String getSlot() { return slot; }
    // Establece el slot del equipo
    public void setSlot(String slot) { this.slot = slot; }

    // Obtiene el ID del ítem
    public Integer getItemId() { return itemId; }
    // Establece el ID del ítem
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    // Obtiene el nombre del ítem
    public String getNombreItem() { return nombreItem; }
    // Establece el nombre del ítem
    public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

    // Obtiene el nivel de ítem (ilvl)
    public Integer getIlvl() { return ilvl; }
    // Establece el nivel de ítem (ilvl)
    public void setIlvl(Integer ilvl) { this.ilvl = ilvl; }

    // Obtiene el personaje asociado al equipo
    public Personaje getPersonaje() { return personaje; }
    // Establece el personaje asociado al equipo
    public void setPersonaje(Personaje personaje) { this.personaje = personaje; }

    @Column(name = "icono")
    private String icono;

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }
}
