package com.biblioteca.auth.domain.model.gateway;

public interface NotificationGateway {

    /**
     * Envia un correo electronico a traves del microservicio de notificacion.
     *
     * @param idUsuario   identificador (UUID) del usuario en auth
     * @param destinatario correo electronico destino
     * @param asunto       asunto del correo
     * @param mensaje      cuerpo del correo (texto o HTML)
     * @param tipo         tipo de notificacion (p. ej. RECUPERACION_PASSWORD)
     */
    void enviarCorreo(String idUsuario,
                      String destinatario,
                      String asunto,
                      String mensaje,
                      String tipo);
}
