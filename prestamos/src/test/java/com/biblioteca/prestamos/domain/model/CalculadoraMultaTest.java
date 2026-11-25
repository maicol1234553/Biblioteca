package com.biblioteca.prestamos.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraMultaTest {

    private final LocalDateTime vencimiento = LocalDateTime.of(2026, 1, 10, 10, 0);

    @Test
    void diasDeRetraso_ceroCuandoNoHayRetraso() {
        assertEquals(0, CalculadoraMulta.diasDeRetraso(vencimiento, vencimiento.minusDays(1)));
        assertEquals(0, CalculadoraMulta.diasDeRetraso(vencimiento, vencimiento));
        assertEquals(0, CalculadoraMulta.diasDeRetraso(null, vencimiento));
        assertEquals(0, CalculadoraMulta.diasDeRetraso(vencimiento, null));
    }

    @Test
    void diasDeRetraso_cuentaDiasIniciados() {
        assertEquals(1, CalculadoraMulta.diasDeRetraso(
                vencimiento, vencimiento.plusSeconds(1)));
        assertEquals(1, CalculadoraMulta.diasDeRetraso(
                vencimiento, vencimiento.plusDays(1)));
        assertEquals(2, CalculadoraMulta.diasDeRetraso(
                vencimiento, vencimiento.plusDays(1).plusSeconds(1)));
    }

    @Test
    void valorMulta_multiplicaDiasPorValor() {
        assertEquals(0, BigDecimal.ZERO.compareTo(
                CalculadoraMulta.valorMulta(0, new BigDecimal("2000"))));
        assertEquals(0, BigDecimal.ZERO.compareTo(
                CalculadoraMulta.valorMulta(2, null)));
        assertEquals(0, new BigDecimal("6000").compareTo(
                CalculadoraMulta.valorMulta(3, new BigDecimal("2000"))));
    }
}
