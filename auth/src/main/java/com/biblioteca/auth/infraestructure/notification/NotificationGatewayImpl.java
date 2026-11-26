package com.biblioteca.auth.infraestructure.notification;

import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NotificationGatewayImpl implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificationGatewayImpl.class);
    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_BASE_MS = 500L;

    private final RestClient notificationRestClient;

    public NotificationGatewayImpl(@Qualifier("notificationRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarCorreo(String idUsuario,
                             String destinatario,
                             String asunto,
                             String mensaje,
                             String tipo) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se envio el correo: el destinatario esta vacio");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idUsuario", idUsuario);
        body.put("destinatario", destinatario);
        body.put("asunto", asunto);
        body.put("mensaje", mensaje);
        body.put("tipo", tipo);

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                notificationRestClient.post()
                        .uri("/notificaciones/enviar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RestClientResponseException exception) {
                int estado = exception.getStatusCode().value();
                boolean reintentable = estado == 429 || estado >= 500;
                log.warn("Notificacion por correo fallo HTTP {} (intento {}/{}): {}",
                        estado, intento, MAX_INTENTOS, exception.getResponseBodyAsString());
                if (!reintentable || intento == MAX_INTENTOS) {
                    break;
                }
            } catch (RestClientException exception) {
                log.warn("No se pudo contactar al servicio de notificacion (intento {}/{}): {}",
                        intento, MAX_INTENTOS, exception.getMessage());
                if (intento == MAX_INTENTOS) {
                    break;
                }
            }

            esperar(intento);
        }

        log.warn("No se pudo enviar la notificacion por correo para {}", idUsuario);
    }

    private void esperar(int intento) {
        try {
            Thread.sleep(ESPERA_BASE_MS * intento);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
