package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.DevolucionUseCase;
import com.biblioteca.prestamos.domain.usecase.MultaUseCase;
import com.biblioteca.prestamos.domain.usecase.PrestamoUseCase;
import com.biblioteca.prestamos.domain.usecase.RenovacionUseCase;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ActualizarVencimientoRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolucionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolverPrestamoRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MultaResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.PrestamoResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.RenovacionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservarLibroRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservaResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.SolicitarPrestamoRequest;
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
class PrestamoControllerTest {

    @Mock
    private PrestamoUseCase prestamoUseCase;

    @Mock
    private ReservaUseCase reservaUseCase;

    @Mock
    private RenovacionUseCase renovacionUseCase;

    @Mock
    private MultaUseCase multaUseCase;

    @Mock
    private DevolucionUseCase devolucionUseCase;

    @InjectMocks
    private PrestamoController prestamoController;

    @Test
    void solicitarPrestamo_debeRetornarPrestamoCreado() {

        SolicitarPrestamoRequest request = mock(SolicitarPrestamoRequest.class);
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(request.idUsuario()).thenReturn("usuario-1");
        when(request.idEjemplar()).thenReturn("ejemplar-1");
        when(prestamoUseCase.solicitarPrestamo("usuario-1", "ejemplar-1", solicitante))
                .thenReturn(prestamo);


        ResponseEntity<PrestamoResponse> respuesta =
                prestamoController.solicitarPrestamo(request, solicitante);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(prestamoUseCase).solicitarPrestamo("usuario-1", "ejemplar-1", solicitante);
    }

    @Test
    void devolverPrestamo_debeRetornarDevolucionCreada() {

        String idPrestamo = "prestamo-1";
        DevolverPrestamoRequest request = mock(DevolverPrestamoRequest.class);
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(request.estadoEjemplar()).thenReturn("DISPONIBLE");
        when(request.observaciones()).thenReturn("Devuelto en buen estado");
        when(prestamoUseCase.devolverPrestamo(
                idPrestamo, "DISPONIBLE", "Devuelto en buen estado", solicitante))
                .thenReturn(devolucion);


        ResponseEntity<DevolucionResponse> respuesta =
                prestamoController.devolverPrestamo(idPrestamo, request, solicitante);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(prestamoUseCase).devolverPrestamo(
                idPrestamo, "DISPONIBLE", "Devuelto en buen estado", solicitante);
    }

    @Test
    void reservarLibro_debeRetornarReservaCreada() {

        String libroId = "libro-1";
        ReservarLibroRequest request = mock(ReservarLibroRequest.class);
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Reserva reserva = mock(Reserva.class);

        when(request.idUsuario()).thenReturn("usuario-1");
        when(reservaUseCase.reservarLibro(libroId, "usuario-1", solicitante))
                .thenReturn(reserva);


        ResponseEntity<ReservaResponse> respuesta =
                prestamoController.reservarLibro(libroId, request, solicitante);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(reservaUseCase).reservarLibro(libroId, "usuario-1", solicitante);
    }

    @Test
    void renovarPrestamo_debeRetornarPrestamoRenovado() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(renovacionUseCase.renovarPrestamo(idPrestamo, solicitante))
                .thenReturn(prestamo);


        ResponseEntity<PrestamoResponse> respuesta =
                prestamoController.renovarPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(renovacionUseCase).renovarPrestamo(idPrestamo, solicitante);
    }

    @Test
    void consultarMisPrestamos_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.consultarMisPrestamos(usuarioId, solicitante))
                .thenReturn(List.of(prestamo));


        ResponseEntity<List<PrestamoResponse>> respuesta =
                prestamoController.consultarMisPrestamos(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(prestamoUseCase).consultarMisPrestamos(usuarioId, solicitante);
    }

    @Test
    void consultarHistorial_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.consultarHistorial(usuarioId, solicitante))
                .thenReturn(List.of(prestamo));


        ResponseEntity<List<PrestamoResponse>> respuesta =
                prestamoController.consultarHistorial(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(prestamoUseCase).consultarHistorial(usuarioId, solicitante);
    }

    @Test
    void consultarMultasPendientes_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.consultarMultasPendientesPorUsuario(usuarioId, solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                prestamoController.consultarMultasPendientes(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).consultarMultasPendientesPorUsuario(usuarioId, solicitante);
    }

    @Test
    void consultarPrestamosVencidos_debeRetornarListaYEstadoOk() {

        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.consultarPrestamosVencidos(solicitante))
                .thenReturn(List.of(prestamo));


        ResponseEntity<List<PrestamoResponse>> respuesta =
                prestamoController.consultarPrestamosVencidos(solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(prestamoUseCase).consultarPrestamosVencidos(solicitante);
    }

    @Test
    void listarPrestamos_debeRetornarListaYEstadoOk() {

        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.listarPrestamos(solicitante))
                .thenReturn(List.of(prestamo));


        ResponseEntity<List<PrestamoResponse>> respuesta =
                prestamoController.listarPrestamos(solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(prestamoUseCase).listarPrestamos(solicitante);
    }

    @Test
    void buscarPrestamo_debeRetornarPrestamoYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.buscarPrestamoPorId(idPrestamo, solicitante))
                .thenReturn(prestamo);


        ResponseEntity<PrestamoResponse> respuesta =
                prestamoController.buscarPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(prestamoUseCase).buscarPrestamoPorId(idPrestamo, solicitante);
    }

    @Test
    void consultarDevolucion_debeRetornarDevolucionYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionUseCase.buscarDevolucionPorPrestamo(idPrestamo, solicitante))
                .thenReturn(devolucion);


        ResponseEntity<DevolucionResponse> respuesta =
                prestamoController.consultarDevolucion(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(devolucionUseCase).buscarDevolucionPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void consultarMultasDelPrestamo_debeRetornarListaYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.listarMultasPorPrestamo(idPrestamo, solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                prestamoController.consultarMultasDelPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).listarMultasPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void consultarRenovacionesDelPrestamo_debeRetornarListaYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionUseCase.listarRenovacionesPorPrestamo(idPrestamo, solicitante))
                .thenReturn(List.of(renovacion));


        ResponseEntity<List<RenovacionResponse>> respuesta =
                prestamoController.consultarRenovacionesDelPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(renovacionUseCase).listarRenovacionesPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void actualizarVencimiento_debeRetornarPrestamoActualizado() {

        String idPrestamo = "prestamo-1";
        ActualizarVencimientoRequest request = mock(ActualizarVencimientoRequest.class);
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.actualizarVencimiento(idPrestamo, null, solicitante))
                .thenReturn(prestamo);


        ResponseEntity<PrestamoResponse> respuesta =
                prestamoController.actualizarVencimiento(idPrestamo, request, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(prestamoUseCase).actualizarVencimiento(idPrestamo, null, solicitante);
    }

    @Test
    void cancelarPrestamo_debeRetornarPrestamoCancelado() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoUseCase.cancelarPrestamo(idPrestamo, solicitante))
                .thenReturn(prestamo);


        ResponseEntity<PrestamoResponse> respuesta =
                prestamoController.cancelarPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(prestamoUseCase).cancelarPrestamo(idPrestamo, solicitante);
    }

    @Test
    void eliminarPrestamo_debeRetornarMensajeYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);


        ResponseEntity<MensajeResponse> respuesta =
                prestamoController.eliminarPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Prestamo eliminado con exito", respuesta.getBody().mensaje());
        verify(prestamoUseCase).eliminarPrestamo(idPrestamo, solicitante);
    }
}