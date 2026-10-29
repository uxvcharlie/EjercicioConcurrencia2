package com.example.ejercicioconcurrencia2;

public class Evento {

    private String hora;
    private String estado;
    private String detalle;

    public Evento(String hora, String estado, String detalle) {
        this.hora = hora;
        this.estado = estado;
        this.detalle = detalle;
    }

    public String getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getDetalle() {
        return detalle;
    }
}
