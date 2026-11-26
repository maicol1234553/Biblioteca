package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.infraestructure.driver_adapters.RecordatorioData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordatorioMapperTest {

    private final RecordatorioMapper mapper = new RecordatorioMapper();

    @Test
    void toRecordatorio_cuandoDataEsNull_debeRetornarNull() {
        // Act
        Recordatorio resultado = mapper.toRecordatorio(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toRecordatorio_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDateTime fechaProgramada =
                LocalDateTime.of(2026, 12, 10, 9, 30);

        RecordatorioData data = mock(RecordatorioData.class);
        when(data.getIdRecordatorio()).thenReturn("recordatorio-1");
        when(data.getIdUsuario()).thenReturn("usuario-1");
        when(data.getIdPrestamo()).thenReturn("prestamo-1");
        when(data.getFechaProgramada()).thenReturn(fechaProgramada);
        when(data.getTipo()).thenReturn("VENCIMIENTO");
        when(data.getEstado()).thenReturn("PENDIENTE");

        // Act
        Recordatorio resultado = mapper.toRecordatorio(data);

        // Assert
        assertAll(
                () -> assertEquals("recordatorio-1", resultado.getIdRecordatorio()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals(fechaProgramada, resultado.getFechaProgramada()),
                () -> assertEquals("VENCIMIENTO", resultado.getTipo()),
                () -> assertEquals("PENDIENTE", resultado.getEstado())
        );
    }

    @Test
    void toRecordatorioData_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDateTime fechaProgramada =
                LocalDateTime.of(2026, 12, 10, 9, 30);

        Recordatorio recordatorio = mock(Recordatorio.class);
        when(recordatorio.getIdRecordatorio()).thenReturn("recordatorio-1");
        when(recordatorio.getIdUsuario()).thenReturn("usuario-1");
        when(recordatorio.getIdPrestamo()).thenReturn("prestamo-1");
        when(recordatorio.getFechaProgramada()).thenReturn(fechaProgramada);
        when(recordatorio.getTipo()).thenReturn("VENCIMIENTO");
        when(recordatorio.getEstado()).thenReturn("PENDIENTE");

        // Act
        RecordatorioData resultado = mapper.toRecordatorioData(recordatorio);

        // Assert
        assertAll(
                () -> assertEquals("recordatorio-1", resultado.getIdRecordatorio()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals(fechaProgramada, resultado.getFechaProgramada()),
                () -> assertEquals("VENCIMIENTO", resultado.getTipo()),
                () -> assertEquals("PENDIENTE", resultado.getEstado())
        );
    }

    @Test
    void toRecordatorioData_cuandoRecordatorioEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toRecordatorioData(null)
        );
    }
}