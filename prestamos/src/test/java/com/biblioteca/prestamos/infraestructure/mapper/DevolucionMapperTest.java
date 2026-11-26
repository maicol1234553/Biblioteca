package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.infraestructure.driver_adapters.DevolucionData;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevolucionMapperTest {

    private final DevolucionMapper mapper = new DevolucionMapper();

    @Test
    void toDevolucion_cuandoDataEsNull_debeRetornarNull() {
        // Act
        Devolucion resultado = mapper.toDevolucion(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toDevolucion_debeMapearLosCamposYElIdDelPrestamo() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 26, 10, 30);

        PrestamoData prestamo = mock(PrestamoData.class);
        when(prestamo.getIdPrestamo()).thenReturn("prestamo-1");

        DevolucionData data = mock(DevolucionData.class);
        when(data.getIdDevolucion()).thenReturn("devolucion-1");
        when(data.getPrestamo()).thenReturn(prestamo);
        when(data.getFechaDevolucion()).thenReturn(fecha);
        when(data.getObservaciones()).thenReturn("Devuelto en buen estado");
        when(data.getEstadoEjemplar()).thenReturn("BUENO");

        // Act
        Devolucion resultado = mapper.toDevolucion(data);

        // Assert
        assertAll(
                () -> assertEquals("devolucion-1", resultado.getIdDevolucion()),
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals(fecha, resultado.getFechaDevolucion()),
                () -> assertEquals(
                        "Devuelto en buen estado",
                        resultado.getObservaciones()
                ),
                () -> assertEquals("BUENO", resultado.getEstadoEjemplar())
        );
    }

    @Test
    void toDevolucion_cuandoPrestamoEsNull_debeDejarIdPrestamoEnNull() {
        // Arrange
        DevolucionData data = mock(DevolucionData.class);
        when(data.getPrestamo()).thenReturn(null);

        // Act
        Devolucion resultado = mapper.toDevolucion(data);

        // Assert
        assertNull(resultado.getIdPrestamo());
    }

    @Test
    void toDevolucionData_debeMapearLosCamposYAsignarElPrestamo() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 26, 10, 30);

        Devolucion devolucion = mock(Devolucion.class);
        when(devolucion.getIdDevolucion()).thenReturn("devolucion-1");
        when(devolucion.getFechaDevolucion()).thenReturn(fecha);
        when(devolucion.getObservaciones()).thenReturn("Devuelto en buen estado");
        when(devolucion.getEstadoEjemplar()).thenReturn("BUENO");

        PrestamoData prestamo = mock(PrestamoData.class);

        // Act
        DevolucionData resultado = mapper.toDevolucionData(devolucion, prestamo);

        // Assert
        assertAll(
                () -> assertEquals("devolucion-1", resultado.getIdDevolucion()),
                () -> assertSame(prestamo, resultado.getPrestamo()),
                () -> assertEquals(fecha, resultado.getFechaDevolucion()),
                () -> assertEquals(
                        "Devuelto en buen estado",
                        resultado.getObservaciones()
                ),
                () -> assertEquals("BUENO", resultado.getEstadoEjemplar())
        );
    }

    @Test
    void toDevolucionData_cuandoDevolucionEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toDevolucionData(null, null)
        );
    }
}