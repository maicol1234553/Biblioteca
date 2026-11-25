package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum EstadoRecordatorio {
    PENDIENTE,
    PROCESADO,
    CANCELADO;

    public static EstadoRecordatorio desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado del recordatorio es obligatorio");
        }

        try {
            return EstadoRecordatorio.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado del recordatorio debe ser PENDIENTE, PROCESADO o CANCELADO");
        }
    }
}
