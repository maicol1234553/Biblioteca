package com.biblioteca.auth.domain.model.gateway;

public interface NotificationGateway {

    void enviarCorreo(String destinatario, String asunto, String mensaje);

    void enviarSms(String telefono, String mensaje);
}
