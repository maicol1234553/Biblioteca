package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.DevolucionUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolucionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
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
class DevolucionControllerTest {

    @Mock
    private DevolucionUseCase devolucionUseCase;

    @InjectMocks
    private DevolucionController devolucionController;

    @Test
    void listarDevoluciones_debeRetornarListaYEstadoOk() {
        // Arrange
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionUseCase.listarDevoluciones(solicitante))
                .thenReturn(List.of(devolucion));

        // Act
        ResponseEntity<List<DevolucionResponse>> respuesta =
                devolucionController.listarDevoluciones(solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(devolucionUseCase).listarDevoluciones(solicitante);
    }

    @Test
    void buscarDevolucion_debeRetornarDevolucionYEstadoOk() {
        // Arrange
        String id = "devolucion-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionUseCase.buscarDevolucion(id, solicitante))
                .thenReturn(devolucion);

        // Act
        ResponseEntity<DevolucionResponse> respuesta =
                devolucionController.buscarDevolucion(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(devolucionUseCase).buscarDevolucion(id, solicitante);
    }

    @Test
    void listarDevolucionesPorPrestamo_debeRetornarListaYEstadoOk() {
        // Arrange
        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionUseCase.listarDevolucionesPorPrestamo(idPrestamo, solicitante))
                .thenReturn(List.of(devolucion));

        // Act
        ResponseEntity<List<DevolucionResponse>> respuesta =
                devolucionController.listarDevolucionesPorPrestamo(idPrestamo, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(devolucionUseCase)
                .listarDevolucionesPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void eliminarDevolucion_debeRetornarMensajeYEstadoOk() {
        // Arrange
        String id = "devolucion-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);

        // Act
        ResponseEntity<MensajeResponse> respuesta =
                devolucionController.eliminarDevolucion(id, solicitante);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Devolucion eliminada con exito",
                respuesta.getBody().mensaje()
        );
        verify(devolucionUseCase).eliminarDevolucion(id, solicitante);
    }
}