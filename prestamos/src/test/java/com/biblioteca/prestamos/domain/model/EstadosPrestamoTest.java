package com.biblioteca.prestamos.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadosPrestamoTest {

    @Test
    void estadoPrestamoNormalizaYReconoceActivos() {
        assertEquals(EstadoPrestamo.ACTIVO, EstadoPrestamo.desde(" activo "));
        assertEquals(EstadoPrestamo.DEVUELTO, EstadoPrestamo.desde("devuelto"));
        assertTrue(EstadoPrestamo.ACTIVO.estaActivo());
        assertTrue(EstadoPrestamo.VENCIDO.estaActivo());
        assertFalse(EstadoPrestamo.DEVUELTO.estaActivo());
        assertFalse(EstadoPrestamo.CANCELADO.estaActivo());
        assertThrows(IllegalArgumentException.class, () -> EstadoPrestamo.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoPrestamo.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoPrestamo.desde("OTRO"));
    }

    @Test
    void estadoReservaNormalizaYRechazaInvalidos() {
        assertEquals(EstadoReserva.PENDIENTE, EstadoReserva.desde(" pendiente "));
        assertEquals(EstadoReserva.EXPIRADA, EstadoReserva.desde("expirada"));
        assertThrows(IllegalArgumentException.class, () -> EstadoReserva.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoReserva.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoReserva.desde("OTRO"));
    }

    @Test
    void estadoRenovacionNormalizaYRechazaInvalidos() {
        assertEquals(EstadoRenovacion.APROBADA, EstadoRenovacion.desde(" aprobada "));
        assertEquals(EstadoRenovacion.RECHAZADA, EstadoRenovacion.desde("rechazada"));
        assertThrows(IllegalArgumentException.class, () -> EstadoRenovacion.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoRenovacion.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoRenovacion.desde("OTRO"));
    }

    @Test
    void estadoMultaNormalizaYRechazaInvalidos() {
        assertEquals(EstadoMulta.PENDIENTE, EstadoMulta.desde(" pendiente "));
        assertEquals(EstadoMulta.PAGADA, EstadoMulta.desde("pagada"));
        assertThrows(IllegalArgumentException.class, () -> EstadoMulta.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoMulta.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoMulta.desde("OTRO"));
    }
}
