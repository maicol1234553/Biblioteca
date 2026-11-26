package com.biblioteca.notificacion.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notificaciones")
public record NotificacionProperties(
        String remitente,
        Reintentos reintentos,
        Job job,
        Recordatorio recordatorio,
        Servicios servicios,
        CorreoApi correoApi
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
     * Envio de correo por API HTTP (funciona en Render Free, donde los puertos
     * SMTP estan bloqueados). Si no hay ninguna api-key, se usa SMTP.
     *
     * - SENDGRID_API_KEY: clave de SendGrid (Twilio). Empieza por "SG.".
     * - BREVO_API_KEY: clave de Brevo. Empieza por "xkeysib-".
     *
     * SendGrid tiene prioridad si ambas estan configuradas.
     */
    public record CorreoApi(String sendgridApiKey,
                            String sendgridUrl,
                            String brevoApiKey,
                            String brevoUrl) {
    }
}
