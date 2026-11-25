package com.biblioteca.catalogo.domain.model;

import java.util.Locale;

public enum EstadoEjemplar {
    DISPONIBLE,
    PRESTADO,
    RESERVADO,
    MANTENIMIENTO,
    PERDIDO;

    public static EstadoEjemplar desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return DISPONIBLE;
        }

        String normalizado = valor.trim().toUpperCase(Locale.ROOT);

        if (normalizado.equals("EN_MANTENIMIENTO") || normalizado.equals("EN MANTENIMIENTO")) {
            return MANTENIMIENTO;
        }

        try {
            return EstadoEjemplar.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado debe ser DISPONIBLE, PRESTADO, RESERVADO, MANTENIMIENTO o PERDIDO");
        }
    }
}
