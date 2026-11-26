package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.model.EventoNotificacion;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.model.TipoNotificacion;
import com.biblioteca.notificacion.domain.usecase.NotificacionUseCase;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.AvisoVencidoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.EnviarCorreoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.EventoNotificacionRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.LibroDisponibleRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.PrestamoConfirmadoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.RecordatorioDevolucionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionUseCase notificacionUseCase;

    @Mock
    private RecordatorioUseCase recordatorioUseCase;

    @InjectMocks
    private NotificacionController notificacionController;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacion = crearNotificacion();
    }



    @Test
    void enviarCorreo_RequestValido_RetornaNotificacionYEstado201() {

        EnviarCorreoRequest request = mock(EnviarCorreoRequest.class);

        when(request.idUsuario()).thenReturn("USR-1");
        when(request.destinatario()).thenReturn("usuario@biblioteca.com");
        when(request.asunto()).thenReturn("Aviso");
        when(request.mensaje()).thenReturn("Mensaje de prueba");
        when(request.tipo())
                .thenReturn(TipoNotificacion.PRESTAMO_CONFIRMADO.name());

        when(notificacionUseCase.enviarCorreo(
                "USR-1",
                "usuario@biblioteca.com",
                "Aviso",
                "Mensaje de prueba",
                TipoNotificacion.PRESTAMO_CONFIRMADO.name()
        )).thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.enviarCorreo(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarCorreo(
                "USR-1",
                "usuario@biblioteca.com",
                "Aviso",
                "Mensaje de prueba",
                TipoNotificacion.PRESTAMO_CONFIRMADO.name()
        );
    }



    @Test
    void programarRecordatorioDevolucion_RequestValido_RetornaEstado201() {

        RecordatorioDevolucionRequest request =
                mock(RecordatorioDevolucionRequest.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(request.idUsuario()).thenReturn("USR-1");
        when(request.idPrestamo()).thenReturn("PREST-1");
        when(request.tipo()).thenReturn("DEVOLUCION");

        when(recordatorioUseCase.programarRecordatorioDevolucion(
                "USR-1",
                "PREST-1",
                null,
                "DEVOLUCION"
        )).thenReturn(recordatorio);


        ResponseEntity<?> respuesta =
                notificacionController.programarRecordatorioDevolucion(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(recordatorioUseCase).programarRecordatorioDevolucion(
                "USR-1",
                "PREST-1",
                null,
                "DEVOLUCION"
        );
    }



    @Test
    void enviarPrestamoConfirmado_RequestValido_RetornaEstado201() {

        PrestamoConfirmadoRequest request =
                mock(PrestamoConfirmadoRequest.class);

        when(request.idUsuario()).thenReturn("USR-1");
        when(request.idPrestamo()).thenReturn("PREST-1");
        when(request.destinatario())
                .thenReturn("usuario@biblioteca.com");
        when(request.mensaje()).thenReturn("Préstamo confirmado");

        when(notificacionUseCase.enviarPrestamoConfirmado(
                "USR-1",
                "PREST-1",
                "usuario@biblioteca.com",
                "Préstamo confirmado"
        )).thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.enviarPrestamoConfirmado(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarPrestamoConfirmado(
                "USR-1",
                "PREST-1",
                "usuario@biblioteca.com",
                "Préstamo confirmado"
        );
    }

    @Test
    void enviarLibroDisponible_RequestValido_RetornaEstado201() {

        LibroDisponibleRequest request =
                mock(LibroDisponibleRequest.class);

        when(request.idUsuario()).thenReturn("USR-1");
        when(request.idLibro()).thenReturn("LIB-1");
        when(request.destinatario())
                .thenReturn("usuario@biblioteca.com");
        when(request.mensaje()).thenReturn("Libro disponible");

        when(notificacionUseCase.enviarLibroDisponible(
                "USR-1",
                "LIB-1",
                "usuario@biblioteca.com",
                "Libro disponible"
        )).thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.enviarLibroDisponible(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarLibroDisponible(
                "USR-1",
                "LIB-1",
                "usuario@biblioteca.com",
                "Libro disponible"
        );
    }

    @Test
    void enviarAvisoVencido_RequestValido_RetornaEstado201() {

        AvisoVencidoRequest request = mock(AvisoVencidoRequest.class);

        when(request.idUsuario()).thenReturn("USR-1");
        when(request.idPrestamo()).thenReturn("PREST-1");
        when(request.destinatario())
                .thenReturn("usuario@biblioteca.com");
        when(request.mensaje()).thenReturn("Préstamo vencido");

        when(notificacionUseCase.enviarAvisoVencido(
                "USR-1",
                "PREST-1",
                "usuario@biblioteca.com",
                "Préstamo vencido"
        )).thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.enviarAvisoVencido(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarAvisoVencido(
                "USR-1",
                "PREST-1",
                "usuario@biblioteca.com",
                "Préstamo vencido"
        );
    }



    @Test
    void recibirEvento_RequestValido_CreaEventoYRetornaEstado201() {

        EventoNotificacionRequest request =
                mock(EventoNotificacionRequest.class);

        when(request.tipo())
                .thenReturn(TipoNotificacion.PRESTAMO_CONFIRMADO.name());
        when(request.idUsuario()).thenReturn("USR-1");
        when(request.referenciaId()).thenReturn("PREST-1");
        when(request.mensaje()).thenReturn("Préstamo confirmado");

        when(notificacionUseCase.enviarEvento(any(EventoNotificacion.class)))
                .thenReturn(notificacion);

        ArgumentCaptor<EventoNotificacion> eventoCaptor =
                ArgumentCaptor.forClass(EventoNotificacion.class);


        ResponseEntity<?> respuesta =
                notificacionController.recibirEvento(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarEvento(eventoCaptor.capture());

        EventoNotificacion eventoEnviado = eventoCaptor.getValue();

        assertEquals(
                TipoNotificacion.PRESTAMO_CONFIRMADO.name(),
                eventoEnviado.tipo()
        );
        assertEquals("USR-1", eventoEnviado.idUsuario());
        assertEquals("PREST-1", eventoEnviado.referenciaId());
        assertEquals("Préstamo confirmado", eventoEnviado.mensaje());
    }



    @Test
    void reintentarNotificacion_IdValido_RetornaNotificacionYEstado200() {

        String id = "NOT-1";

        when(notificacionUseCase.reintentarNotificacion(id))
                .thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.reintentarNotificacion(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(notificacionUseCase).reintentarNotificacion(id);
    }



    @Test
    void reintentarFallidas_RetornaListaYEstado200() {

        when(notificacionUseCase.reintentarFallidas())
                .thenReturn(List.of(notificacion));


        ResponseEntity<?> respuesta =
                notificacionController.reintentarFallidas();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).reintentarFallidas();
    }



    @Test
    void consultarHistorialUsuario_IdValido_RetornaListaYEstado200() {

        String idUsuario = "USR-1";

        when(notificacionUseCase.consultarHistorialUsuario(idUsuario))
                .thenReturn(List.of(notificacion));


        ResponseEntity<?> respuesta =
                notificacionController.consultarHistorialUsuario(idUsuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarHistorialUsuario(idUsuario);
    }



    @Test
    void consultarPendientes_RetornaListaYEstado200() {

        when(notificacionUseCase.consultarPendientes())
                .thenReturn(List.of(notificacion));


        ResponseEntity<?> respuesta =
                notificacionController.consultarPendientes();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarPendientes();
    }



    @Test
    void listarNotificaciones_RetornaListaYEstado200() {

        when(notificacionUseCase.listarNotificaciones())
                .thenReturn(List.of(notificacion));


        ResponseEntity<?> respuesta =
                notificacionController.listarNotificaciones();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).listarNotificaciones();
    }



    @Test
    void buscarNotificacion_IdValido_RetornaNotificacionYEstado200() {

        String id = "NOT-1";

        when(notificacionUseCase.buscarNotificacion(id))
                .thenReturn(notificacion);


        ResponseEntity<?> respuesta =
                notificacionController.buscarNotificacion(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(notificacionUseCase).buscarNotificacion(id);
    }



    @Test
    void consultarHistorialEnvios_IdValido_RetornaListaYEstado200() {

        String id = "NOT-1";
        HistorialEnvio historial = mock(HistorialEnvio.class);

        when(notificacionUseCase.consultarHistorialEnvios(id))
                .thenReturn(List.of(historial));


        ResponseEntity<?> respuesta =
                notificacionController.consultarHistorialEnvios(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarHistorialEnvios(id);
    }



    @Test
    void eliminarNotificacion_IdValido_RetornaMensajeYEstado200() {

        String id = "NOT-1";


        ResponseEntity<?> respuesta =
                notificacionController.eliminarNotificacion(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(notificacionUseCase).eliminarNotificacion(id);
    }

    private Notificacion crearNotificacion() {
        return new Notificacion(
                "NOT-1",
                "USR-1",
                "usuario@biblioteca.com",
                "Asunto de prueba",
                "<p>Mensaje de prueba</p>",
                TipoNotificacion.PRESTAMO_CONFIRMADO.name(),
                LocalDateTime.of(2026, 1, 15, 10, 15, 30),
                null,
                "PENDIENTE"
        );
    }
}