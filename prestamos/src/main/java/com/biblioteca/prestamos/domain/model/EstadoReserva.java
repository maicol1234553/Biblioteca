package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

public enum EstadoReserva {
    PENDIENTE,
    CONVERTIDA,
    CANCELADA,
    EXPIRADA;

    public static EstadoReserva desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la reserva es obligatorio");
        }

        try {
            return EstadoReserva.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado de la reserva debe ser PENDIENTE, CONVERTIDA, CANCELADA o EXPIRADA");
        }
    }
}
