package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.infraestructure.driver_adapters.HistorialEnvioData;
import com.biblioteca.notificacion.infraestructure.driver_adapters.NotificacionData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistorialEnvioMapperTest {

    private final HistorialEnvioMapper mapper = new HistorialEnvioMapper();

    @Test
    void toHistorial_cuandoDataEsNull_debeRetornarNull() {

        HistorialEnvio resultado = mapper.toHistorial(null);


        assertNull(resultado);
    }

    @Test
    void toHistorial_debeMapearLosCamposYElIdDeLaNotificacion() {

        LocalDateTime fecha = LocalDateTime.of(2026, 11, 26, 10, 30);

        NotificacionData notificacion = mock(NotificacionData.class);
        when(notificacion.getIdNotificacion()).thenReturn("notificacion-1");

        HistorialEnvioData data = mock(HistorialEnvioData.class);
        when(data.getIdHistorial()).thenReturn("historial-1");
        when(data.getNotificacion()).thenReturn(notificacion);
        when(data.getFechaIntento()).thenReturn(fecha);
        when(data.getResultado()).thenReturn("ENVIADO");
        when(data.getMensajeError()).thenReturn(null);


        HistorialEnvio resultado = mapper.toHistorial(data);


        assertAll(
                () -> assertEquals("historial-1", resultado.getIdHistorial()),
                () -> assertEquals(
                        "notificacion-1",
                        resultado.getIdNotificacion()
                ),
                () -> assertEquals(fecha, resultado.getFechaIntento()),
                () -> assertEquals("ENVIADO", resultado.getResultado()),
                () -> assertNull(resultado.getMensajeError())
        );
    }

    @Test
    void toHistorial_cuandoNoTieneNotificacion_debeDejarElIdEnNull() {

        HistorialEnvioData data = mock(HistorialEnvioData.class);
        when(data.getNotificacion()).thenReturn(null);


        HistorialEnvio resultado = mapper.toHistorial(data);


        assertNull(resultado.getIdNotificacion());
    }

    @Test
    void toHistorialData_debeMapearLosCamposYAsignarLaNotificacion() {

        LocalDateTime fecha = LocalDateTime.of(2026, 11, 26, 10, 30);

        HistorialEnvio historial = mock(HistorialEnvio.class);
        when(historial.getIdHistorial()).thenReturn("historial-1");
        when(historial.getFechaIntento()).thenReturn(fecha);
        when(historial.getResultado()).thenReturn("FALLIDO");
        when(historial.getMensajeError()).thenReturn("No se pudo enviar");

        NotificacionData notificacion = mock(NotificacionData.class);


        HistorialEnvioData resultado =
                mapper.toHistorialData(historial, notificacion);


        assertAll(
                () -> assertEquals("historial-1", resultado.getIdHistorial()),
                () -> assertSame(notificacion, resultado.getNotificacion()),
                () -> assertEquals(fecha, resultado.getFechaIntento()),
                () -> assertEquals("FALLIDO", resultado.getResultado()),
                () -> assertEquals(
                        "No se pudo enviar",
                        resultado.getMensajeError()
                )
        );
    }

    @Test
    void toHistorialData_cuandoHistorialEsNull_debeLanzarNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> mapper.toHistorialData(null, null)
        );
    }
}