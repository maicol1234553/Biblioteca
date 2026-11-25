package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import com.biblioteca.prestamos.domain.model.Devolucion;

import java.time.LocalDateTime;

public record DevolucionResponse(
        String idDevolucion,
        String idPrestamo,
        LocalDateTime fechaDevolucion,
        String observaciones,
        String estadoEjemplar
) {

    public static DevolucionResponse from(Devolucion devolucion) {
        return new DevolucionResponse(
                devolucion.getIdDevolucion(),
                devolucion.getIdPrestamo(),
                devolucion.getFechaDevolucion(),
                devolucion.getObservaciones(),
                devolucion.getEstadoEjemplar()
        );
    }
}
