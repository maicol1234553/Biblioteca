package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.exception.PlantillaNoEncontradaException;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.usecase.PlantillaUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.PlantillaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantillaControllerTest {

    @Mock
    private PlantillaUseCase plantillaUseCase;

    @InjectMocks
    private PlantillaController plantillaController;

    private PlantillaNotificacion plantilla;

    @BeforeEach
    void setUp() {
        plantilla = crearPlantilla();
    }

    // LISTAR PLANTILLAS

    @Test
    void listarPlantillas_RetornaListaYEstado200() {
        // Arrange
        when(plantillaUseCase.listarPlantillas())
                .thenReturn(List.of(plantilla));

        // Act
        ResponseEntity<?> respuesta =
                plantillaController.listarPlantillas();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
        verify(plantillaUseCase).listarPlantillas();
    }

    // BUSCAR PLANTILLA

    @Test
    void buscarPlantilla_PlantillaExiste_RetornaRespuestaYEstado200() {
        // Arrange
        String id = "PLANT-1";

        when(plantillaUseCase.buscarPlantilla(id))
                .thenReturn(plantilla);

        // Act
        ResponseEntity<?> respuesta =
                plantillaController.buscarPlantilla(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(plantillaUseCase).buscarPlantilla(id);
    }

    @Test
    void buscarPlantilla_PlantillaNoExiste_PropagaExcepcion() {
        // Arrange
        String id = "PLANT-INEXISTENTE";

        when(plantillaUseCase.buscarPlantilla(id))
                .thenThrow(new PlantillaNoEncontradaException(
                        "Plantilla no encontrada con id: " + id
                ));

        // Act
        PlantillaNoEncontradaException excepcion = assertThrows(
                PlantillaNoEncontradaException.class,
                () -> plantillaController.buscarPlantilla(id)
        );

        // Assert
        assertEquals(
                "Plantilla no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(plantillaUseCase).buscarPlantilla(id);
    }

    // CREAR PLANTILLA

    @Test
    void crearPlantilla_RequestValido_MapeaDatosYRetornaEstado201() {
        // Arrange
        PlantillaRequest request = mock(PlantillaRequest.class);

        when(request.nombre()).thenReturn("Préstamo confirmado");
        when(request.asunto()).thenReturn("Confirmación");
        when(request.contenido()).thenReturn("<p>Préstamo confirmado</p>");
        when(request.tipo()).thenReturn("PRESTAMO_CONFIRMADO");
        when(request.estado()).thenReturn("ACTIVA");

        when(plantillaUseCase.crearPlantilla(any(PlantillaNotificacion.class)))
                .thenReturn(plantilla);

        ArgumentCaptor<PlantillaNotificacion> captor =
                ArgumentCaptor.forClass(PlantillaNotificacion.class);

        // Act
        ResponseEntity<?> respuesta =
                plantillaController.crearPlantilla(request);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(plantillaUseCase).crearPlantilla(captor.capture());

        PlantillaNotificacion enviada = captor.getValue();

        assertEquals("Préstamo confirmado", enviada.getNombre());
        assertEquals("Confirmación", enviada.getAsunto());
        assertEquals("<p>Préstamo confirmado</p>", enviada.getContenido());
        assertEquals("PRESTAMO_CONFIRMADO", enviada.getTipo());
        assertEquals("ACTIVA", enviada.getEstado());
    }

    // ACTUALIZAR PLANTILLA

    @Test
    void actualizarPlantilla_RequestValido_PasaIdYPlantillaYRetornaEstado200() {
        // Arrange
        String id = "PLANT-1";
        PlantillaRequest request = mock(PlantillaRequest.class);

        when(request.nombre()).thenReturn("Nombre actualizado");
        when(request.asunto()).thenReturn("Asunto actualizado");
        when(request.contenido()).thenReturn("<p>Contenido actualizado</p>");
        when(request.tipo()).thenReturn("PRESTAMO_CONFIRMADO");
        when(request.estado()).thenReturn("ACTIVA");

        when(plantillaUseCase.actualizarPlantilla(
                eq(id),
                any(PlantillaNotificacion.class)
        )).thenReturn(plantilla);

        ArgumentCaptor<PlantillaNotificacion> captor =
                ArgumentCaptor.forClass(PlantillaNotificacion.class);

        // Act
        ResponseEntity<?> respuesta =
                plantillaController.actualizarPlantilla(id, request);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        verify(plantillaUseCase)
                .actualizarPlantilla(eq(id), captor.capture());

        PlantillaNotificacion enviada = captor.getValue();

        assertEquals("Nombre actualizado", enviada.getNombre());
        assertEquals("Asunto actualizado", enviada.getAsunto());
        assertEquals(
                "<p>Contenido actualizado</p>",
                enviada.getContenido()
        );
        assertEquals("PRESTAMO_CONFIRMADO", enviada.getTipo());
        assertEquals("ACTIVA", enviada.getEstado());
    }

    // ELIMINAR PLANTILLA

    @Test
    void eliminarPlantilla_IdValido_RetornaMensajeYEstado200() {
        // Arrange
        String id = "PLANT-1";

        // Act
        ResponseEntity<?> respuesta =
                plantillaController.eliminarPlantilla(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Plantilla eliminada con exito",
                ((com.biblioteca.notificacion.infraestructure.entry_points.dto.MensajeResponse)
                        respuesta.getBody()).mensaje()
        );
        verify(plantillaUseCase).eliminarPlantilla(id);
    }

    private PlantillaNotificacion crearPlantilla() {
        PlantillaNotificacion nuevaPlantilla = new PlantillaNotificacion();
        nuevaPlantilla.setIdPlantilla("PLANT-1");
        nuevaPlantilla.setNombre("Préstamo confirmado");
        nuevaPlantilla.setAsunto("Confirmación");
        nuevaPlantilla.setContenido("<p>Préstamo confirmado</p>");
        nuevaPlantilla.setTipo("PRESTAMO_CONFIRMADO");
        nuevaPlantilla.setEstado("ACTIVA");
        return nuevaPlantilla;
    }
}