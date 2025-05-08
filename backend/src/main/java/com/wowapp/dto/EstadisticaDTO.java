package com.wowapp.dto;

public class EstadisticaDTO {
    private String nombre;
    private int valor;

    public EstadisticaDTO(String nombre, int valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public int getValor() {
        return valor;
    }
}
