package com.biblioteca.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoEjemplarTest {

    @Test
    void desdeNormalizaValoresYReconoceMantenimiento() {
        assertEquals(EstadoEjemplar.DISPONIBLE, EstadoEjemplar.desde(null));
        assertEquals(EstadoEjemplar.DISPONIBLE, EstadoEjemplar.desde("  "));
        assertEquals(EstadoEjemplar.PRESTADO, EstadoEjemplar.desde(" prestado "));
        assertEquals(EstadoEjemplar.MANTENIMIENTO, EstadoEjemplar.desde("en_mantenimiento"));
        assertEquals(EstadoEjemplar.MANTENIMIENTO, EstadoEjemplar.desde("en mantenimiento"));
        assertEquals(EstadoEjemplar.PERDIDO, EstadoEjemplar.desde("perdido"));
    }

    @Test
    void desdeRechazaEstadosDesconocidos() {
        assertThrows(IllegalArgumentException.class, () -> EstadoEjemplar.desde("ROTO"));
    }
}
