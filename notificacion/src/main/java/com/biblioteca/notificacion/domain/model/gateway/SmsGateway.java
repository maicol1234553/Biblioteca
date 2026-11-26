package com.biblioteca.notificacion.domain.model.gateway;

/**
 * Puerto de envio de mensajes de texto. El destinatario debe llegar en
 * formato E.164 (+573001234567) y el mensaje en texto plano.
 */
public interface SmsGateway {

    void enviar(String telefono, String mensaje);
}
