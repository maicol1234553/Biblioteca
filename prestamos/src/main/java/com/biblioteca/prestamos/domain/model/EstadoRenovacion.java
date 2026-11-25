package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

public enum EstadoRenovacion {
    APROBADA,
    RECHAZADA;

    public static EstadoRenovacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la renovacion es obligatorio");
        }

        try {
            return EstadoRenovacion.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado de la renovacion debe ser APROBADA o RECHAZADA");
        }
    }
}
