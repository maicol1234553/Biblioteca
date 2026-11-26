package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.infraestructure.driver_adapters.MultaData;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultaMapperTest {

    private final MultaMapper mapper = new MultaMapper();

    @Test
    void toMulta_cuandoDataEsNull_debeRetornarNull() {

        Multa resultado = mapper.toMulta(null);


        assertNull(resultado);
    }

    @Test
    void toMulta_debeMapearLosCamposYElIdDelPrestamo() {

        BigDecimal valor = new BigDecimal("25000.00");
        LocalDateTime fechaGeneracion =
                LocalDateTime.of(2026, 11, 26, 10, 30);

        PrestamoData prestamo = mock(PrestamoData.class);
        when(prestamo.getIdPrestamo()).thenReturn("prestamo-1");

        MultaData data = mock(MultaData.class);
        when(data.getIdMulta()).thenReturn("multa-1");
        when(data.getPrestamo()).thenReturn(prestamo);
        when(data.getValor()).thenReturn(valor);
        when(data.getMotivo()).thenReturn("Retraso en la devolución");
        when(data.getEstado()).thenReturn("PENDIENTE");
        when(data.getFechaGeneracion()).thenReturn(fechaGeneracion);


        Multa resultado = mapper.toMulta(data);


        assertAll(
                () -> assertEquals("multa-1", resultado.getIdMulta()),
                () -> assertEquals("prestamo-1", resultado.getIdPrestamo()),
                () -> assertEquals(valor, resultado.getValor()),
                () -> assertEquals(
                        "Retraso en la devolución",
                        resultado.getMotivo()
                ),
                () -> assertEquals("PENDIENTE", resultado.getEstado()),
                () -> assertEquals(
                        fechaGeneracion,
                        resultado.getFechaGeneracion()
                )
        );
    }

    @Test
    void toMulta_cuandoPrestamoEsNull_debeDejarIdPrestamoEnNull() {

        MultaData data = mock(MultaData.class);
        when(data.getPrestamo()).thenReturn(null);


        Multa resultado = mapper.toMulta(data);


        assertNull(resultado.getIdPrestamo());
    }

    @Test
    void toMultaData_debeMapearLosCamposYAsignarElPrestamo() {

        BigDecimal valor = new BigDecimal("25000.00");
        LocalDateTime fechaGeneracion =
                LocalDateTime.of(2026, 11, 26, 10, 30);

        Multa multa = mock(Multa.class);
        when(multa.getIdMulta()).thenReturn("multa-1");
        when(multa.getValor()).thenReturn(valor);
        when(multa.getMotivo()).thenReturn("Retraso en la devolución");
        when(multa.getEstado()).thenReturn("PENDIENTE");
        when(multa.getFechaGeneracion()).thenReturn(fechaGeneracion);

        PrestamoData prestamo = mock(PrestamoData.class);


        MultaData resultado = mapper.toMultaData(multa, prestamo);


        assertAll(
                () -> assertEquals("multa-1", resultado.getIdMulta()),
                () -> assertSame(prestamo, resultado.getPrestamo()),
                () -> assertEquals(valor, resultado.getValor()),
                () -> assertEquals(
                        "Retraso en la devolución",
                        resultado.getMotivo()
                ),
                () -> assertEquals("PENDIENTE", resultado.getEstado()),
                () -> assertEquals(
                        fechaGeneracion,
                        resultado.getFechaGeneracion()
                )
        );
    }

    @Test
    void toMultaData_cuandoMultaEsNull_debeLanzarNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> mapper.toMultaData(null, null)
        );
    }
}