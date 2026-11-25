package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

public enum EstadoPrestamo {
    ACTIVO,
    VENCIDO,
    DEVUELTO,
    CANCELADO;

    public static EstadoPrestamo desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado del prestamo es obligatorio");
        }

        try {
            return EstadoPrestamo.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado del prestamo debe ser ACTIVO, VENCIDO, DEVUELTO o CANCELADO");
        }
    }

    public boolean estaActivo() {
        return this == ACTIVO || this == VENCIDO;
    }
}
