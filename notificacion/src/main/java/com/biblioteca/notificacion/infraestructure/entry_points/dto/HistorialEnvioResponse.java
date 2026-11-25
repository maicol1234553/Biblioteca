package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import com.biblioteca.notificacion.domain.model.HistorialEnvio;

import java.time.LocalDateTime;

public record HistorialEnvioResponse(
        String idHistorial,
        String idNotificacion,
        LocalDateTime fechaIntento,
        String resultado,
        String mensajeError
) {

    public static HistorialEnvioResponse from(HistorialEnvio historial) {
        return new HistorialEnvioResponse(
                historial.getIdHistorial(),
                historial.getIdNotificacion(),
                historial.getFechaIntento(),
                historial.getResultado(),
                historial.getMensajeError()
        );
    }
}
