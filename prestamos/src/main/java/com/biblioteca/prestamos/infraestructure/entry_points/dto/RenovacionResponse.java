package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import com.biblioteca.prestamos.domain.model.Renovacion;

import java.time.LocalDateTime;

public record RenovacionResponse(
        String idRenovacion,
        String idPrestamo,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaAnterior,
        LocalDateTime fechaNueva,
        String estado
) {

    public static RenovacionResponse from(Renovacion renovacion) {
        return new RenovacionResponse(
                renovacion.getIdRenovacion(),
                renovacion.getIdPrestamo(),
                renovacion.getFechaSolicitud(),
                renovacion.getFechaAnterior(),
                renovacion.getFechaNueva(),
                renovacion.getEstado()
        );
    }
}
