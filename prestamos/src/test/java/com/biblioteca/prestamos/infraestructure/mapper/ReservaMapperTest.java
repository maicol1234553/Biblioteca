package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.infraestructure.driver_adapters.ReservaData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservaMapperTest {

    private final ReservaMapper mapper = new ReservaMapper();

    @Test
    void toReserva_cuandoDataEsNull_debeRetornarNull() {
        assertNull(mapper.toReserva(null));
    }

    @Test
    void toReserva_debeCopiarTodosLosCampos() {
        LocalDateTime fechaReserva = LocalDateTime.of(2026, 11, 20, 9, 0);
        LocalDateTime fechaExpiracion = LocalDateTime.of(2026, 11, 23, 9, 0);

        ReservaData data = mock(ReservaData.class);
        when(data.getIdReserva()).thenReturn("reserva-1");
        when(data.getIdUsuario()).thenReturn("usuario-1");
        when(data.getIdLibro()).thenReturn("libro-1");
        when(data.getFechaReserva()).thenReturn(fechaReserva);
        when(data.getFechaExpiracion()).thenReturn(fechaExpiracion);
        when(data.getEstado()).thenReturn("PENDIENTE");

        Reserva resultado = mapper.toReserva(data);

        assertAll(
                () -> assertEquals("reserva-1", resultado.getIdReserva()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("libro-1", resultado.getIdLibro()),
                () -> assertEquals(fechaReserva, resultado.getFechaReserva()),
                () -> assertEquals(fechaExpiracion, resultado.getFechaExpiracion()),
                () -> assertEquals("PENDIENTE", resultado.getEstado())
        );
    }

    @Test
    void toReservaData_debeCopiarTodosLosCampos() {
        LocalDateTime fechaReserva = LocalDateTime.of(2026, 11, 20, 9, 0);
        LocalDateTime fechaExpiracion = LocalDateTime.of(2026, 11, 23, 9, 0);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getIdReserva()).thenReturn("reserva-1");
        when(reserva.getIdUsuario()).thenReturn("usuario-1");
        when(reserva.getIdLibro()).thenReturn("libro-1");
        when(reserva.getFechaReserva()).thenReturn(fechaReserva);
        when(reserva.getFechaExpiracion()).thenReturn(fechaExpiracion);
        when(reserva.getEstado()).thenReturn("PENDIENTE");

        ReservaData resultado = mapper.toReservaData(reserva);

        assertAll(
                () -> assertEquals("reserva-1", resultado.getIdReserva()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("libro-1", resultado.getIdLibro()),
                () -> assertEquals(fechaReserva, resultado.getFechaReserva()),
                () -> assertEquals(fechaExpiracion, resultado.getFechaExpiracion()),
                () -> assertEquals("PENDIENTE", resultado.getEstado())
        );
    }

    @Test
    void toReservaData_cuandoReservaEsNull_debeLanzarNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.toReservaData(null));
    }
}
