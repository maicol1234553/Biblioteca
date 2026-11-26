package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.RenovacionUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.RenovacionResponse;
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
class RenovacionControllerTest {

    @Mock
    private RenovacionUseCase renovacionUseCase;

    @InjectMocks
    private RenovacionController renovacionController;

    @Test
    void listarRenovaciones_debeRetornarListaYEstadoOk() {
        // Arrange
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionUseCase.listarRenovaciones(solicitante))
                .thenReturn(List.of(renovacion));

        // Act
        ResponseEntity<List<RenovacionResponse>> respuesta =
                renovacionController.listarRenovaciones(solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(renovacionUseCase).listarRenovaciones(solicitante);
    }

    @Test
    void buscarRenovacion_debeRetornarRenovacionYEstadoOk() {
        // Arrange
        String id = "renovacion-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionUseCase.buscarRenovacion(id, solicitante))
                .thenReturn(renovacion);

        // Act
        ResponseEntity<RenovacionResponse> respuesta =
                renovacionController.buscarRenovacion(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(renovacionUseCase).buscarRenovacion(id, solicitante);
    }

    @Test
    void listarRenovacionesPorPrestamo_debeRetornarListaYEstadoOk() {
        // Arrange
        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionUseCase.listarRenovacionesPorPrestamo(idPrestamo, solicitante))
                .thenReturn(List.of(renovacion));

        // Act
        ResponseEntity<List<RenovacionResponse>> respuesta =
                renovacionController.listarRenovacionesPorPrestamo(idPrestamo, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(renovacionUseCase).listarRenovacionesPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void eliminarRenovacion_debeRetornarMensajeYEstadoOk() {
        // Arrange
        String id = "renovacion-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);

        // Act
        ResponseEntity<MensajeResponse> respuesta =
                renovacionController.eliminarRenovacion(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Renovacion eliminada con exito",
                respuesta.getBody().mensaje()
        );
        verify(renovacionUseCase).eliminarRenovacion(id, solicitante);
    }
}