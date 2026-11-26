package com.biblioteca.auth.infraestructure.notification;

import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envia correos llamando al microservicio de notificacion.
 *
 * El contrato real de /notificaciones/enviar-correo acepta JSON con los campos
 * idUsuario (UUID), destinatario, asunto, mensaje y tipo. Un fallo del servicio
 * de notificacion se registra en el log y no interrumpe la operacion de auth.
 */
@Service
public class NotificationGatewayImpl implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificationGatewayImpl.class);

    private final RestClient notificationRestClient;

    public NotificationGatewayImpl(@Qualifier("notificationRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

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

        try {
            notificationRestClient.post()
                    .uri("/notificaciones/enviar-correo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("Notificacion por correo fallo ({}): {}",
                    exception.getStatusCode().value(), exception.getResponseBodyAsString());
        } catch (RestClientException exception) {
            log.warn("No se pudo contactar al servicio de notificacion: {}", exception.getMessage());
        }
    }
}
