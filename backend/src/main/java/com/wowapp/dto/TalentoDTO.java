package com.wowapp.dto;

public class TalentoDTO {
    private String nombre;
    private int spellId;
    private String icono;

    public TalentoDTO(String nombre, int spellId, String icono) {
        this.nombre = nombre;
        this.spellId = spellId;
        this.icono = icono;
    }

    public String getNombre() {
        return nombre;
    }

    public int getSpellId() {
        return spellId;
    }

    public String getIcono() {
        return icono;
    }

    private String tipo;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    private String wowheadUrl;

    public TalentoDTO(String nombre, Integer spellId, String icono, String tipo) {
        this.nombre = nombre;
        this.spellId = spellId;
        this.icono = icono;
        this.tipo = tipo;
        this.wowheadUrl = "https://www.wowhead.com/es/spell=" + (spellId != null ? spellId : 0);
    }

    public String getWowheadUrl() {
        return wowheadUrl;
    }
}
