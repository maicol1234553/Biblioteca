package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum TipoRecordatorio {
    DEVOLUCION_PROXIMA,
    PRESTAMO_VENCIDO;

    public static TipoRecordatorio desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return DEVOLUCION_PROXIMA;
        }

        String normalizado = valor.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        try {
            return TipoRecordatorio.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El tipo de recordatorio debe ser DEVOLUCION_PROXIMA o PRESTAMO_VENCIDO");
        }
    }
}
