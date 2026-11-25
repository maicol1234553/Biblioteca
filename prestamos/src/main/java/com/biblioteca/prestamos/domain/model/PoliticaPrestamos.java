package com.biblioteca.prestamos.domain.model;

import java.math.BigDecimal;

public record PoliticaPrestamos(
        int limitePrestamosActivos,
        int diasPrestamo,
        int diasReserva,
        int maxRenovaciones,
        BigDecimal multaPorDia
) {
}
