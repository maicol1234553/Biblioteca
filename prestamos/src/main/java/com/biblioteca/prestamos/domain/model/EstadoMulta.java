package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

public enum EstadoMulta {
    PENDIENTE,
    PAGADA,
    ANULADA;

    public static EstadoMulta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la multa es obligatorio");
        }

        try {
            return EstadoMulta.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado de la multa debe ser PENDIENTE, PAGADA o ANULADA");
        }
    }
}
