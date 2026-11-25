package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import com.biblioteca.prestamos.domain.model.Multa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MultaResponse(
        String idMulta,
        String idPrestamo,
        BigDecimal valor,
        String motivo,
        String estado,
        LocalDateTime fechaGeneracion
) {

    public static MultaResponse from(Multa multa) {
        return new MultaResponse(
                multa.getIdMulta(),
                multa.getIdPrestamo(),
                multa.getValor(),
                multa.getMotivo(),
                multa.getEstado(),
                multa.getFechaGeneracion()
        );
    }
}
