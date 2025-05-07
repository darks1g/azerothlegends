package com.wowapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Genera automáticamente el ID único para cada usuario
    private Long id;

    @Column(nullable = false, unique = true, length = 100) // El correo electrónico debe ser único y no puede ser nulo
    private String email;

    @Column(nullable = false) // Define el tipo de usuario: "web" o "battlenet"
    private String tipo;

    // Nombre de usuario, opcional para usuarios de tipo "battlenet"
    private String nombreUsuario;

    // Battletag, solo aplicable para usuarios de tipo "battlenet"
    private String battletag;

    // Hash de la contraseña, solo para usuarios de tipo "web"
    private String passwordHash;

    // Indica si el usuario ha sido verificado
    private Boolean esVerificado = false;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL) // Relación uno a muchos con la entidad Personaje
    private List<Personaje> personajes;

    // Métodos getter y setter para acceder y modificar los atributos

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
