package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrestamoMapperTest {

    private final PrestamoMapper mapper = new PrestamoMapper();

    @Test
    void toPrestamo_cuandoDataEsNull_debeRetornarNull() {
        // Act
        Prestamo resultado = mapper.toPrestamo(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toPrestamo_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDateTime fechaPrestamo =
                LocalDateTime.of(2026, 11, 20, 9, 0);
        LocalDateTime fechaVencimiento =
                LocalDateTime.of(2026, 12, 20, 9, 0);
        LocalDateTime fechaDevolucion =
                LocalDateTime.of(2026, 11, 25, 14, 30);

        PrestamoData data = mock(PrestamoData.class);
        when(data.getIdPrestamo()).thenReturn("prestamo-1");
        when(data.getIdUsuario()).thenReturn("usuario-1");
        when(data.getIdEjemplar()).thenReturn("ejemplar-1");
        when(data.getFechaPrestamo()).thenReturn(fechaPrestamo);
        when(data.getFechaVencimiento()).thenReturn(fechaVencimiento);
        when(data.getFechaDevolucion()).thenReturn(fechaDevolucion);
        when(data.getEstado()).thenReturn("DEVUELTO");

        // Act
        Prestamo resultado = mapper.toPrestamo(data);

        // Assert
        assertAll(
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("ejemplar-1", resultado.getIdEjemplar()),
                () -> assertEquals(fechaPrestamo, resultado.getFechaPrestamo()),
                () -> assertEquals(
                        fechaVencimiento,
                        resultado.getFechaVencimiento()
                ),
                () -> assertEquals(
                        fechaDevolucion,
                        resultado.getFechaDevolucion()
                ),
                () -> assertEquals("DEVUELTO", resultado.getEstado())
        );
    }

    @Test
    void toPrestamoData_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDateTime fechaPrestamo =
                LocalDateTime.of(2026, 11, 20, 9, 0);
        LocalDateTime fechaVencimiento =
                LocalDateTime.of(2026, 12, 20, 9, 0);
        LocalDateTime fechaDevolucion =
                LocalDateTime.of(2026, 11, 25, 14, 30);

        Prestamo prestamo = mock(Prestamo.class);
        when(prestamo.getIdPrestamo()).thenReturn("prestamo-1");
        when(prestamo.getIdUsuario()).thenReturn("usuario-1");
        when(prestamo.getIdEjemplar()).thenReturn("ejemplar-1");
        when(prestamo.getFechaPrestamo()).thenReturn(fechaPrestamo);
        when(prestamo.getFechaVencimiento()).thenReturn(fechaVencimiento);
        when(prestamo.getFechaDevolucion()).thenReturn(fechaDevolucion);
        when(prestamo.getEstado()).thenReturn("DEVUELTO");

        // Act
        PrestamoData resultado = mapper.toPrestamoData(prestamo);

        // Assert
        assertAll(
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("ejemplar-1", resultado.getIdEjemplar()),
                () -> assertEquals(fechaPrestamo, resultado.getFechaPrestamo()),
                () -> assertEquals(
                        fechaVencimiento,
                        resultado.getFechaVencimiento()
                ),
                () -> assertEquals(
                        fechaDevolucion,
                        resultado.getFechaDevolucion()
                ),
                () -> assertEquals("DEVUELTO", resultado.getEstado())
        );
    }

    @Test
    void toPrestamoData_cuandoPrestamoEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toPrestamoData(null)
        );
    }
}