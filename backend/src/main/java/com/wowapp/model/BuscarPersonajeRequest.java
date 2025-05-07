package com.wowapp.model;

import com.wowapp.model.Personaje.VersionJuego;

/**
 * Clase que representa una solicitud para buscar un personaje.
 * Contiene información como el nombre, reino, región y versión del juego.
 */
public class BuscarPersonajeRequest {
    private String nombre; // Nombre del personaje
    private String reino; // Reino al que pertenece el personaje
    private String region; // Región del personaje
    private VersionJuego versionJuego; // Versión del juego del personaje

    // Getters y Setters

    /**
     * Obtiene el nombre del personaje.
     * @return Nombre del personaje.
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre del personaje.
     * @param nombre Nombre del personaje.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el reino del personaje.
     * @return Reino del personaje.
     */
    public String getReino() { return reino; }

    /**
     * Establece el reino del personaje.
     * @param reino Reino del personaje.
     */
    public void setReino(String reino) { this.reino = reino; }

    /**
     * Obtiene la región del personaje.
     * @return Región del personaje.
     */
    public String getRegion() { return region; }

    /**
     * Establece la región del personaje.
     * @param region Región del personaje.
     */
    public void setRegion(String region) { this.region = region; }

    /**
     * Obtiene la versión del juego del personaje.
     * @return Versión del juego del personaje.
     */
    public VersionJuego getVersionJuego() { return versionJuego; }

    /**
     * Establece la versión del juego del personaje.
     * @param versionJuego Versión del juego del personaje.
     */
    public void setVersionJuego(VersionJuego versionJuego) { this.versionJuego = versionJuego; }
}
