package com.example.ejercicioconcurrencia2;

public enum EstadoApp {
    LISTO("Listo"), EN_EJECUCION("En ejecucion"),
    CANCELANDO("Cancelando"), CANCELADO("Cancelado"),
    COMPLETADO("Completado"), ERROR("Error");
    
    private final String texto;

    EstadoApp(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }
}
