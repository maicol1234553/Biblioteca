package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum ResultadoEnvio {
    EXITOSO,
    FALLIDO;

    public static ResultadoEnvio desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El resultado del envio es obligatorio");
        }

        try {
            return ResultadoEnvio.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El resultado del envio debe ser EXITOSO o FALLIDO");
        }
    }
}
