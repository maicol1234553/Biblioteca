package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificacionGatewayImpl implements NotificacionGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionGatewayImpl.class);

    private final RestClient notificacionRestClient;
    private final PrestamosProperties properties;

    public NotificacionGatewayImpl(
            @Qualifier("notificacionRestClient") RestClient notificacionRestClient,
            PrestamosProperties properties) {
        this.notificacionRestClient = notificacionRestClient;
        this.properties = properties;
    }

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

        try {
            notificacionRestClient.post()
                    .uri(properties.servicios().notificacionPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("No se pudo enviar la notificacion {} para {}: {}",
                    evento.tipo(), evento.idUsuario(), exception.getMessage());
        }
    }
}
