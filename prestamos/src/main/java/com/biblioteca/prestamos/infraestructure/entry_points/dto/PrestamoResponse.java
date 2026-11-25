package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import com.biblioteca.prestamos.domain.model.Prestamo;

import java.time.LocalDateTime;

public record PrestamoResponse(
        String idPrestamo,
        String idUsuario,
        String idEjemplar,
        LocalDateTime fechaPrestamo,
        LocalDateTime fechaVencimiento,
        LocalDateTime fechaDevolucion,
        String estado
) {

    public static PrestamoResponse from(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getIdPrestamo(),
                prestamo.getIdUsuario(),
                prestamo.getIdEjemplar(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                prestamo.getFechaDevolucion(),
                prestamo.getEstado()
        );
    }
}
