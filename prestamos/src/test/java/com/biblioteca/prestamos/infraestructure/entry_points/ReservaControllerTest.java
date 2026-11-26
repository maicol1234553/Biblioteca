package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private ReservaUseCase reservaUseCase;

    @InjectMocks
    private ReservaController reservaController;

    @Test
    void listarReservas_debeRetornarListaYEstadoOk() {
        // Arrange
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.listarReservas(solicitante))
                .thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservas(solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).listarReservas(solicitante);
    }

    @Test
    void buscarReserva_debeRetornarReservaYEstadoOk() {
        // Arrange
        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.buscarReserva(id, solicitante))
                .thenReturn(reserva);

        // Act
        ResponseEntity<ReservaResponse> respuesta =
                reservaController.buscarReserva(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(reservaUseCase).buscarReserva(id, solicitante);
    }

    @Test
    void listarReservasPorUsuario_debeRetornarListaYEstadoOk() {
        // Arrange
        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.consultarReservasPorUsuario(usuarioId, solicitante))
                .thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservasPorUsuario(usuarioId, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).consultarReservasPorUsuario(usuarioId, solicitante);
    }

    @Test
    void listarReservasPendientesPorLibro_debeRetornarListaYEstadoOk() {
        // Arrange
        String libroId = "libro-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.consultarReservasPendientesPorLibro(libroId, solicitante))
                .thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservasPendientesPorLibro(libroId, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase)
                .consultarReservasPendientesPorLibro(libroId, solicitante);
    }

    @Test
    void cancelarReserva_debeRetornarReservaCanceladaYEstadoOk() {
        // Arrange
        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.cancelarReserva(id, solicitante))
                .thenReturn(reserva);

        // Act
        ResponseEntity<ReservaResponse> respuesta =
                reservaController.cancelarReserva(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(reservaUseCase).cancelarReserva(id, solicitante);
    }

    @Test
    void expirarReservas_debeRetornarReservasExpiradasYEstadoOk() {
        // Arrange
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.expirarReservas(solicitante))
                .thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.expirarReservas(solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).expirarReservas(solicitante);
    }

    @Test
    void eliminarReserva_debeRetornarMensajeYEstadoOk() {
        // Arrange
        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);

        // Act
        ResponseEntity<MensajeResponse> respuesta =
                reservaController.eliminarReserva(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Reserva eliminada con exito", respuesta.getBody().mensaje());
        verify(reservaUseCase).eliminarReserva(id, solicitante);
    }
}