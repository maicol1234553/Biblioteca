package com.biblioteca.prestamos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;




public final class CalculadoraMulta {

    private CalculadoraMulta() {
    }

    public static long diasDeRetraso(LocalDateTime fechaVencimiento, LocalDateTime fechaEntrega) {
        if (fechaVencimiento == null || fechaEntrega == null || !fechaEntrega.isAfter(fechaVencimiento)) {
            return 0;
        }

        long dias = ChronoUnit.DAYS.between(fechaVencimiento, fechaEntrega);

        if (fechaVencimiento.plusDays(dias).isBefore(fechaEntrega)) {
            dias++;
        }

        return Math.max(1, dias);
    }

    public static BigDecimal valorMulta(long dias, BigDecimal valorPorDia) {
        if (dias <= 0 || valorPorDia == null) {
            return BigDecimal.ZERO;
        }

        return valorPorDia.multiply(BigDecimal.valueOf(dias));
    }
}
