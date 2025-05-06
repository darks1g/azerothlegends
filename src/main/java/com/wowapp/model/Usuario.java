package com.wowapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String tipo; // "web" o "battlenet"

    private String nombreUsuario; // Solo para usuarios web (opcional para battlenet)

    private String battletag; // Solo para usuarios battlenet

    // Para usuarios web, se puede guardar hash de contraseña
    private String passwordHash;

    private Boolean esVerificado = false;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Personaje> personajes;

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getBattletag() {
        return battletag;
    }

    public void setBattletag(String battletag) {
        this.battletag = battletag;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEsVerificado() {
        return esVerificado;
    }

    public void setEsVerificado(Boolean esVerificado) {
        this.esVerificado = esVerificado;
    }

    public List<Personaje> getPersonajes() {
        return personajes;
    }

    public void setPersonajes(List<Personaje> personajes) {
        this.personajes = personajes;
    }
}
