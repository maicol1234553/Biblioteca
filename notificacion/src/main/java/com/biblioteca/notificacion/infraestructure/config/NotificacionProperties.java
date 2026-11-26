package com.biblioteca.notificacion.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notificaciones")
public record NotificacionProperties(
        String remitente,
        Reintentos reintentos,
        Job job,
        Recordatorio recordatorio,
        Servicios servicios,
        Sms sms,
        String canal
) {

    public record Reintentos(int maximos, long esperaMs) {
    }

    public record Job(long intervaloMs) {
    }

    public record Recordatorio(int diasAnticipacion) {
    }

    public record Servicios(String authUrl, int timeoutSegundos) {
    }

    /**
     * Envio de mensajes de texto por el proveedor Brevo (ex Sendinblue).
     * Sin api-key configurada el envio SMS queda FALLIDA y es reintentable.
     */
    public record Sms(String apiKey, String sender, String codigoPais, int timeoutSegundos) {
    }
}
