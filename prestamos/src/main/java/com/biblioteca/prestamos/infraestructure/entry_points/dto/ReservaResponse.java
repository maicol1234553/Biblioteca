package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import com.biblioteca.prestamos.domain.model.Reserva;

import java.time.LocalDateTime;

public record ReservaResponse(
        String idReserva,
        String idUsuario,
        String idLibro,
        LocalDateTime fechaReserva,
        LocalDateTime fechaExpiracion,
        String estado
) {

    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(
                reserva.getIdReserva(),
                reserva.getIdUsuario(),
                reserva.getIdLibro(),
                reserva.getFechaReserva(),
                reserva.getFechaExpiracion(),
                reserva.getEstado()
        );
    }
}
