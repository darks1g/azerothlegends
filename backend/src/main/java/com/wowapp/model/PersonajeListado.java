package com.wowapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "personajes_listado",
    uniqueConstraints = @UniqueConstraint(columnNames = {"owner_key", "personaje_id"})
)
public class PersonajeListado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_key", nullable = false, length = 80)
    private String ownerKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    @Column(name = "ultima_busqueda", nullable = false)
    private LocalDateTime ultimaBusqueda;

    public PersonajeListado() {}

    public PersonajeListado(String ownerKey, Personaje personaje, LocalDateTime ultimaBusqueda) {
        this.ownerKey = ownerKey;
        this.personaje = personaje;
        this.ultimaBusqueda = ultimaBusqueda;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public LocalDateTime getUltimaBusqueda() {
        return ultimaBusqueda;
    }

    public void setUltimaBusqueda(LocalDateTime ultimaBusqueda) {
        this.ultimaBusqueda = ultimaBusqueda;
    }
}
