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

    // ENVIAR CORREO

    @Test
    void enviarCorreo_RequestValido_RetornaNotificacionYEstado201() {
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.enviarCorreo(request);

        // Assert
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

    // PROGRAMAR RECORDATORIO DE DEVOLUCIÓN

    @Test
    void programarRecordatorioDevolucion_RequestValido_RetornaEstado201() {
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.programarRecordatorioDevolucion(request);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(recordatorioUseCase).programarRecordatorioDevolucion(
                "USR-1",
                "PREST-1",
                null,
                "DEVOLUCION"
        );
    }

    // NOTIFICACIONES DE PRÉSTAMO, LIBRO DISPONIBLE Y VENCIMIENTO

    @Test
    void enviarPrestamoConfirmado_RequestValido_RetornaEstado201() {
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.enviarPrestamoConfirmado(request);

        // Assert
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
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.enviarLibroDisponible(request);

        // Assert
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
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.enviarAvisoVencido(request);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(notificacionUseCase).enviarAvisoVencido(
                "USR-1",
                "PREST-1",
                "usuario@biblioteca.com",
                "Préstamo vencido"
        );
    }

    // RECIBIR EVENTO

    @Test
    void recibirEvento_RequestValido_CreaEventoYRetornaEstado201() {
        // Arrange
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

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.recibirEvento(request);

        // Assert
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

    // REINTENTAR UNA NOTIFICACIÓN

    @Test
    void reintentarNotificacion_IdValido_RetornaNotificacionYEstado200() {
        // Arrange
        String id = "NOT-1";

        when(notificacionUseCase.reintentarNotificacion(id))
                .thenReturn(notificacion);

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.reintentarNotificacion(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(notificacionUseCase).reintentarNotificacion(id);
    }

    // REINTENTAR NOTIFICACIONES FALLIDAS

    @Test
    void reintentarFallidas_RetornaListaYEstado200() {
        // Arrange
        when(notificacionUseCase.reintentarFallidas())
                .thenReturn(List.of(notificacion));

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.reintentarFallidas();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).reintentarFallidas();
    }

    // CONSULTAR HISTORIAL DEL USUARIO

    @Test
    void consultarHistorialUsuario_IdValido_RetornaListaYEstado200() {
        // Arrange
        String idUsuario = "USR-1";

        when(notificacionUseCase.consultarHistorialUsuario(idUsuario))
                .thenReturn(List.of(notificacion));

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.consultarHistorialUsuario(idUsuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarHistorialUsuario(idUsuario);
    }

    // CONSULTAR NOTIFICACIONES PENDIENTES

    @Test
    void consultarPendientes_RetornaListaYEstado200() {
        // Arrange
        when(notificacionUseCase.consultarPendientes())
                .thenReturn(List.of(notificacion));

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.consultarPendientes();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarPendientes();
    }

    // LISTAR NOTIFICACIONES

    @Test
    void listarNotificaciones_RetornaListaYEstado200() {
        // Arrange
        when(notificacionUseCase.listarNotificaciones())
                .thenReturn(List.of(notificacion));

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.listarNotificaciones();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).listarNotificaciones();
    }

    // BUSCAR NOTIFICACIÓN POR ID

    @Test
    void buscarNotificacion_IdValido_RetornaNotificacionYEstado200() {
        // Arrange
        String id = "NOT-1";

        when(notificacionUseCase.buscarNotificacion(id))
                .thenReturn(notificacion);

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.buscarNotificacion(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(notificacionUseCase).buscarNotificacion(id);
    }

    // CONSULTAR HISTORIAL DE ENVÍOS

    @Test
    void consultarHistorialEnvios_IdValido_RetornaListaYEstado200() {
        // Arrange
        String id = "NOT-1";
        HistorialEnvio historial = mock(HistorialEnvio.class);

        when(notificacionUseCase.consultarHistorialEnvios(id))
                .thenReturn(List.of(historial));

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.consultarHistorialEnvios(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(notificacionUseCase).consultarHistorialEnvios(id);
    }

    // ELIMINAR NOTIFICACIÓN

    @Test
    void eliminarNotificacion_IdValido_RetornaMensajeYEstado200() {
        // Arrange
        String id = "NOT-1";

        // Act
        ResponseEntity<?> respuesta =
                notificacionController.eliminarNotificacion(id);

        // Assert
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