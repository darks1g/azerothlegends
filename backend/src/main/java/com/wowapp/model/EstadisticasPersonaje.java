package com.wowapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name = "estadisticas_personaje")
public class EstadisticasPersonaje {

    // Identificador único de la entidad
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos básicos de las estadísticas del personaje
    private Integer vida;
    private Integer fuerza;
    private Integer agilidad;
    private Integer intelecto;
    private Integer aguante;

    private String nombre;
    private Integer valor;

    public String getNombre() { return nombre; }
    public Integer getValor() { return valor; }

    // Golpe crítico del personaje, mapeado a una columna específica
    @Column(name = "golpe_critico")
    private BigDecimal golpeCritico;

    // Otras estadísticas avanzadas del personaje
    private BigDecimal celeridad;
    private BigDecimal maestria;
    private BigDecimal versatilidad;

    // Relación muchos-a-uno con la entidad Personaje
    @ManyToOne
    @JoinColumn(name = "personaje_id", nullable = false)
    private Personaje personaje;

    // Métodos getter y setter para acceder y modificar los atributos

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getFuerza() {
        return fuerza;
    }
    
    public void setFuerza(Integer fuerza) {
        this.fuerza = fuerza;
    }
    
    public Integer getAgilidad() {
        return agilidad;
    }
    
    public void setAgilidad(Integer agilidad) {
        this.agilidad = agilidad;
    }
    
    public Integer getIntelecto() {
        return intelecto;
    }
    
    public void setIntelecto(Integer intelecto) {
        this.intelecto = intelecto;
    }
    
    public Integer getAguante() {
        return aguante;
    }
    
    public void setAguante(Integer aguante) {
        this.aguante = aguante;
    }
    
    public BigDecimal getGolpeCritico() {
        return golpeCritico;
    }
    
    public void setGolpeCritico(BigDecimal golpeCritico) {
        this.golpeCritico = golpeCritico;
    }
    
    public BigDecimal getCeleridad() {
        return celeridad;
    }
    
    public void setCeleridad(BigDecimal celeridad) {
        this.celeridad = celeridad;
    }
    
    public BigDecimal getMaestria() {
        return maestria;
    }
    
    public void setMaestria(BigDecimal maestria) {
        this.maestria = maestria;
    }
    
    public BigDecimal getVersatilidad() {
        return versatilidad;
    }
    
    public void setVersatilidad(BigDecimal versatilidad) {
        this.versatilidad = versatilidad;
    }
    
    public Personaje getPersonaje() {
        return personaje;
    }
    
    public void setPersonaje(Personaje personaje) {
        this.personaje = personaje;
    }

    public Integer getVida() {
        return vida;
    }

    public void setVida(Integer vida) {
        this.vida = vida;
    }
}
