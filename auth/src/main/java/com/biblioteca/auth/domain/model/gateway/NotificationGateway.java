package com.biblioteca.auth.domain.model.gateway;

public interface NotificationGateway {










    void enviarCorreo(String idUsuario,
                      String destinatario,
                      String asunto,
                      String mensaje,
                      String tipo);
}
