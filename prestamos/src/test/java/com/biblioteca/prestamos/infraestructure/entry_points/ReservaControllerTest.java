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

        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.listarReservas(solicitante))
                .thenReturn(List.of(reserva));


        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservas(solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).listarReservas(solicitante);
    }

    @Test
    void buscarReserva_debeRetornarReservaYEstadoOk() {

        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.buscarReserva(id, solicitante))
                .thenReturn(reserva);


        ResponseEntity<ReservaResponse> respuesta =
                reservaController.buscarReserva(id, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(reservaUseCase).buscarReserva(id, solicitante);
    }

    @Test
    void listarReservasPorUsuario_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.consultarReservasPorUsuario(usuarioId, solicitante))
                .thenReturn(List.of(reserva));


        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservasPorUsuario(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).consultarReservasPorUsuario(usuarioId, solicitante);
    }

    @Test
    void listarReservasPendientesPorLibro_debeRetornarListaYEstadoOk() {

        String libroId = "libro-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.consultarReservasPendientesPorLibro(libroId, solicitante))
                .thenReturn(List.of(reserva));


        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.listarReservasPendientesPorLibro(libroId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase)
                .consultarReservasPendientesPorLibro(libroId, solicitante);
    }

    @Test
    void cancelarReserva_debeRetornarReservaCanceladaYEstadoOk() {

        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.cancelarReserva(id, solicitante))
                .thenReturn(reserva);


        ResponseEntity<ReservaResponse> respuesta =
                reservaController.cancelarReserva(id, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(reservaUseCase).cancelarReserva(id, solicitante);
    }

    @Test
    void expirarReservas_debeRetornarReservasExpiradasYEstadoOk() {

        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaUseCase.expirarReservas(solicitante))
                .thenReturn(List.of(reserva));


        ResponseEntity<List<ReservaResponse>> respuesta =
                reservaController.expirarReservas(solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(reservaUseCase).expirarReservas(solicitante);
    }

    @Test
    void eliminarReserva_debeRetornarMensajeYEstadoOk() {

        String id = "reserva-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);


        ResponseEntity<MensajeResponse> respuesta =
                reservaController.eliminarReserva(id, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Reserva eliminada con exito", respuesta.getBody().mensaje());
        verify(reservaUseCase).eliminarReserva(id, solicitante);
    }
}