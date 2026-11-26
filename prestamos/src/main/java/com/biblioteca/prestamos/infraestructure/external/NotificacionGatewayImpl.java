package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificacionGatewayImpl implements NotificacionGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionGatewayImpl.class);
    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_BASE_MS = 500L;

    private final RestClient notificacionRestClient;
    private final PrestamosProperties properties;

    public NotificacionGatewayImpl(
            @Qualifier("notificacionRestClient") RestClient notificacionRestClient,
            PrestamosProperties properties) {
        this.notificacionRestClient = notificacionRestClient;
        this.properties = properties;
    }

    @Async("notificacionExecutor")
    @Override
    public void enviarEvento(EventoNotificacion evento) {
        if (!properties.servicios().notificacionHabilitada()) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipo", evento.tipo().name());
        payload.put("idUsuario", evento.idUsuario());
        payload.put("referenciaId", evento.referenciaId());
        payload.put("mensaje", evento.mensaje());
        payload.put("fecha", evento.fecha());

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                notificacionRestClient.post()
                        .uri(properties.servicios().notificacionPath())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RestClientResponseException exception) {
                int estado = exception.getStatusCode().value();
                boolean reintentable = estado == 429 || estado >= 500;
                log.warn("Notificacion {} para {} fallo HTTP {} (intento {}/{}): {}",
                        evento.tipo(), evento.idUsuario(), estado, intento, MAX_INTENTOS,
                        exception.getResponseBodyAsString());
                if (!reintentable || intento == MAX_INTENTOS) {
                    break;
                }
            } catch (RestClientException exception) {
                log.warn("No se pudo enviar la notificacion {} para {} (intento {}/{}): {}",
                        evento.tipo(), evento.idUsuario(), intento, MAX_INTENTOS,
                        exception.getMessage());
                if (intento == MAX_INTENTOS) {
                    break;
                }
            }

            esperar(intento);
        }

        log.warn("No se pudo enviar la notificacion {} para {}",
                evento.tipo(), evento.idUsuario());
    }

    private void esperar(int intento) {
        try {
            Thread.sleep(ESPERA_BASE_MS * intento);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
