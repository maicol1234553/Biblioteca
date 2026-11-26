package com.biblioteca.auth.infraestructure.notification;

import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import com.biblioteca.auth.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Service
public class NotificationGatewayImpl implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificationGatewayImpl.class);

    private final RestClient notificationRestClient;
    private final ObjectMapper objectMapper;

    public NotificationGatewayImpl(@Qualifier("notificationRestClient") RestClient notificationRestClient,
                                   ObjectMapper objectMapper) {
        this.notificationRestClient = notificationRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El destinatario es obligatorio para envio de correo");
        }
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("destinatario", destinatario);
            body.add("asunto", asunto);
            body.add("mensaje", mensaje);
            body.add("tipo", "RECUPERACION_PASSWORD");
            body.add("idUsuario", "internal");

            notificationRestClient.post()
                    .uri("/notificaciones/enviar-correo")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("Notificacion por correo fallo ({}): {}", exception.getStatusCode().value(), exception.getMessage());
        } catch (RestClientException exception) {
            log.warn("No se pudo contactar al servicio de notificacion: {}", exception.getMessage());
        }
    }

    @Override
    public void enviarSms(String telefono, String mensaje) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El telefono es obligatorio para envio de SMS");
        }
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("mensaje", mensaje);
            body.add("tipo", "RECUPERACION_PASSWORD");
            body.add("idUsuario", "internal");

            notificationRestClient.post()
                    .uri("/notificaciones/enviar-sms")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("Notificacion por SMS fallo ({}): {}", exception.getStatusCode().value(), exception.getMessage());
        } catch (RestClientException exception) {
            log.warn("No se pudo contactar al servicio de notificacion: {}", exception.getMessage());
        }
    }
}