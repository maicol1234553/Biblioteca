package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum TipoNotificacion {
    PRESTAMO_CONFIRMADO,
    PRESTAMO_REGISTRADO,
    DEVOLUCION_PROXIMA,
    DEVOLUCION_REGISTRADA,
    PRESTAMO_VENCIDO,
    LIBRO_DISPONIBLE,
    RESERVA_REGISTRADA,
    RESERVA_EXPIRADA,
    RENOVACION_REGISTRADA,
    MULTA_GENERADA,
    RECUPERACION_PASSWORD,
    GENERICA;

    public static TipoNotificacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return GENERICA;
        }

        String normalizado = valor.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        try {
            return TipoNotificacion.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            return GENERICA;
        }
    }
}
