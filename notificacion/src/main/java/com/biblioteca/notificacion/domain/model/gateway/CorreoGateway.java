package com.biblioteca.notificacion.domain.model.gateway;

public interface CorreoGateway {

    /**
     * Envia un correo HTML. Lanza DestinatarioInvalidoException si el correo
     * no es valido y EnvioCorreoException si falla el SMTP.
     */
    void enviar(String destinatario, String asunto, String contenidoHtml);
}
