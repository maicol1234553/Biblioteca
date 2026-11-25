package com.biblioteca.prestamos.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "prestamos")
public record PrestamosProperties(
        int limitePrestamosActivos,
        int diasPrestamo,
        int diasReserva,
        int maxRenovaciones,
        BigDecimal multaPorDia,
        Servicios servicios,
        Job job
) {

    public record Servicios(
            String authUrl,
            String catalogoUrl,
            String notificacionUrl,
            String catalogoAdminUsuarioId,
            boolean notificacionHabilitada,
            String notificacionPath,
            int timeoutSegundos
    ) {
    }

    public record Job(long intervaloMs) {
    }
}
