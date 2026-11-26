package com.biblioteca.notificacion.domain.model.gateway;

public interface CorreoGateway {





    void enviar(String destinatario, String asunto, String contenidoHtml);
}
