package com.biblioteca.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisponibilidadLibroTest {

    @Test
    void constructor_debeGuardarTodosLosValores() {
        // Arrange
        List<String> ubicaciones = List.of("Estante A", "Sala 2");

        // Act
        DisponibilidadLibro resultado = new DisponibilidadLibro(
                "libro-1",
                "Cien años de soledad",
                10,
                4,
                3,
                2,
                1,
                0,
                true,
                ubicaciones
        );

        // Assert
        assertAll(
                () -> assertEquals("libro-1", resultado.idLibro()),
                () -> assertEquals("Cien años de soledad", resultado.titulo()),
                () -> assertEquals(10, resultado.totalEjemplares()),
                () -> assertEquals(4, resultado.disponibles()),
                () -> assertEquals(3, resultado.prestados()),
                () -> assertEquals(2, resultado.reservados()),
                () -> assertEquals(1, resultado.enMantenimiento()),
                () -> assertEquals(0, resultado.perdidos()),
                () -> assertTrue(resultado.disponible()),
                () -> assertEquals(ubicaciones, resultado.ubicacionesDisponibles())
        );
    }
}