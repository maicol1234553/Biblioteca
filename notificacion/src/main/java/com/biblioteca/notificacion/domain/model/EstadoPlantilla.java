package com.biblioteca.notificacion.domain.model;

import java.util.Locale;

public enum EstadoPlantilla {
    ACTIVA,
    INACTIVA;

    public static EstadoPlantilla desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return ACTIVA;
        }

        try {
            return EstadoPlantilla.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El estado de la plantilla debe ser ACTIVA o INACTIVA");
        }
    }
}
