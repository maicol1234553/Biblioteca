package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.infraestructure.driver_adapters.NotificacionData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificacionMapperTest {

    private final NotificacionMapper mapper = new NotificacionMapper();

    @Test
    void toNotificacion_cuandoDataEsNull_debeRetornarNull() {

        Notificacion resultado = mapper.toNotificacion(null);


        assertNull(resultado);
    }

    @Test
    void toNotificacion_debeCopiarTodosLosCampos() {

        LocalDateTime fechaCreacion =
                LocalDateTime.of(2026, 11, 26, 9, 0);
        LocalDateTime fechaEnvio =
                LocalDateTime.of(2026, 11, 26, 9, 5);

        NotificacionData data = mock(NotificacionData.class);
        when(data.getIdNotificacion()).thenReturn("notificacion-1");
        when(data.getIdUsuario()).thenReturn("usuario-1");
        when(data.getDestinatario()).thenReturn("persona@correo.com");
        when(data.getAsunto()).thenReturn("Préstamo confirmado");
        when(data.getMensaje()).thenReturn("Tu préstamo fue confirmado");
        when(data.getTipo()).thenReturn("PRESTAMO");
        when(data.getFechaCreacion()).thenReturn(fechaCreacion);
        when(data.getFechaEnvio()).thenReturn(fechaEnvio);
        when(data.getEstado()).thenReturn("ENVIADA");


        Notificacion resultado = mapper.toNotificacion(data);


        assertAll(
                () -> assertEquals("notificacion-1", resultado.getIdNotificacion()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("persona@correo.com", resultado.getDestinatario()),
                () -> assertEquals("Préstamo confirmado", resultado.getAsunto()),
                () -> assertEquals(
                        "Tu préstamo fue confirmado",
                        resultado.getMensaje()
                ),
                () -> assertEquals("PRESTAMO", resultado.getTipo()),
                () -> assertEquals(fechaCreacion, resultado.getFechaCreacion()),
                () -> assertEquals(fechaEnvio, resultado.getFechaEnvio()),
                () -> assertEquals("ENVIADA", resultado.getEstado())
        );
    }

    @Test
    void toNotificacionData_debeCopiarTodosLosCampos() {

        LocalDateTime fechaCreacion =
                LocalDateTime.of(2026, 11, 26, 9, 0);
        LocalDateTime fechaEnvio =
                LocalDateTime.of(2026, 11, 26, 9, 5);

        Notificacion notificacion = mock(Notificacion.class);
        when(notificacion.getIdNotificacion()).thenReturn("notificacion-1");
        when(notificacion.getIdUsuario()).thenReturn("usuario-1");
        when(notificacion.getDestinatario()).thenReturn("persona@correo.com");
        when(notificacion.getAsunto()).thenReturn("Préstamo confirmado");
        when(notificacion.getMensaje()).thenReturn("Tu préstamo fue confirmado");
        when(notificacion.getTipo()).thenReturn("PRESTAMO");
        when(notificacion.getFechaCreacion()).thenReturn(fechaCreacion);
        when(notificacion.getFechaEnvio()).thenReturn(fechaEnvio);
        when(notificacion.getEstado()).thenReturn("ENVIADA");


        NotificacionData resultado = mapper.toNotificacionData(notificacion);


        assertAll(
                () -> assertEquals("notificacion-1", resultado.getIdNotificacion()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("persona@correo.com", resultado.getDestinatario()),
                () -> assertEquals("Préstamo confirmado", resultado.getAsunto()),
                () -> assertEquals(
                        "Tu préstamo fue confirmado",
                        resultado.getMensaje()
                ),
                () -> assertEquals("PRESTAMO", resultado.getTipo()),
                () -> assertEquals(fechaCreacion, resultado.getFechaCreacion()),
                () -> assertEquals(fechaEnvio, resultado.getFechaEnvio()),
                () -> assertEquals("ENVIADA", resultado.getEstado())
        );
    }

    @Test
    void toNotificacionData_cuandoNotificacionEsNull_debeLanzarNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> mapper.toNotificacionData(null)
        );
    }
}