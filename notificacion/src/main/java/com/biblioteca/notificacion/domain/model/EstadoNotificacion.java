package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum EstadoNotificacion {
    PENDIENTE,
    ENVIADA,
    FALLIDA;

    public static EstadoNotificacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El estado de la notificacion es obligatorio");
        }

        try {
            return EstadoNotificacion.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El estado debe ser PENDIENTE, ENVIADA o FALLIDA");
        }
    }
}
