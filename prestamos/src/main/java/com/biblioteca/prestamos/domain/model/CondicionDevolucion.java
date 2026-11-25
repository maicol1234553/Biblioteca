package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

public enum CondicionDevolucion {
    BUENO,
    DANADO,
    PERDIDO;

    public static CondicionDevolucion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return BUENO;
        }

        String normalizado = valor.trim()
                .toUpperCase(Locale.ROOT)
                .replace("Ñ", "N")
                .replace(" ", "_");

        if (normalizado.equals("BUEN_ESTADO") || normalizado.equals("BUEN")) {
            return BUENO;
        }

        if (normalizado.equals("DANADO") || normalizado.equals("DAÑADO")
                || normalizado.equals("MALO") || normalizado.equals("DETERIORADO")) {
            return DANADO;
        }

        try {
            return CondicionDevolucion.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "La condicion del ejemplar debe ser BUENO, DANADO o PERDIDO");
        }
    }

    public String estadoCatalogo() {
        return switch (this) {
            case BUENO -> "DISPONIBLE";
            case DANADO -> "MANTENIMIENTO";
            case PERDIDO -> "PERDIDO";
        };
    }
}
