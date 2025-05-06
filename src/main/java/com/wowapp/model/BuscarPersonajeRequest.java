// BuscarPersonajeRequest.java
package com.wowapp.model;

import com.wowapp.model.Personaje.VersionJuego;

public class BuscarPersonajeRequest {
    private String nombre;
    private String reino;
    private String region;
    private VersionJuego versionJuego;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getReino() { return reino; }
    public void setReino(String reino) { this.reino = reino; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public VersionJuego getVersionJuego() { return versionJuego; }
    public void setVersionJuego(VersionJuego versionJuego) { this.versionJuego = versionJuego; }
}
