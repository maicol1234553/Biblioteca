package com.biblioteca.auth.infraestructure.notification;

import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementacion de notificaciones por consola.
 * Para enviar correos reales, reemplazar esta clase por una que use
 * spring-boot-starter-mail o una API como Brevo, manteniendo la interfaz.
 */
@Service
public class NotificationGatewayImpl implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificationGatewayImpl.class);

    @Override
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        log.info("NOTIFICACION -> para={} | asunto={} | mensaje={}", destinatario, asunto, mensaje);
    }
}
