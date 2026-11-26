package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import com.biblioteca.notificacion.domain.model.Notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        String idNotificacion,
        String idUsuario,
        String destinatario,
        String asunto,
        String mensaje,
        String tipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEnvio,
        String estado,
        String canal
) {

    public static NotificacionResponse from(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getIdNotificacion(),
                notificacion.getIdUsuario(),
                notificacion.getDestinatario(),
                notificacion.getAsunto(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaEnvio(),
                notificacion.getEstado(),
                notificacion.getCanal()
        );
    }
}
