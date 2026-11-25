package com.biblioteca.notificacion.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notificaciones")
public record NotificacionProperties(
        String remitente,
        Reintentos reintentos,
        Job job,
        Recordatorio recordatorio,
        Servicios servicios
) {

    public record Reintentos(int maximos, long esperaMs) {
    }

    public record Job(long intervaloMs) {
    }

    public record Recordatorio(int diasAnticipacion) {
    }

    public record Servicios(String authUrl, int timeoutSegundos) {
    }
}
