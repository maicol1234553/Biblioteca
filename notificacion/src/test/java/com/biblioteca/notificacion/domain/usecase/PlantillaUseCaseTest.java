package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.TestFixtures;
import com.biblioteca.notificacion.domain.exception.PlantillaNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.RecursoDuplicadoException;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantillaUseCaseTest {

    @Mock
    private PlantillaNotificacionGateway plantillaGateway;

    private PlantillaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PlantillaUseCase(plantillaGateway);
    }

    @Test
    void crearPlantilla_generaIdYNormaliza() {
        PlantillaNotificacion plantilla = new PlantillaNotificacion();
        plantilla.setNombre("Plantilla de prueba");
        plantilla.setAsunto("Asunto");
        plantilla.setContenido("<p>Contenido</p>");
        plantilla.setTipo(null);
        plantilla.setEstado(null);
        when(plantillaGateway.guardarPlantilla(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlantillaNotificacion creada = useCase.crearPlantilla(plantilla);

        assertNotNull(creada.getIdPlantilla());
        assertEquals("GENERICA", creada.getTipo());
        assertEquals("ACTIVA", creada.getEstado());
    }

    @Test
    void crearPlantilla_rechazaDuplicados() {
        PlantillaNotificacion plantilla = plantilla("pl1", "GENERICA");
        when(plantillaGateway.buscarPlantillaPorNombre("Plantilla GENERICA"))
                .thenReturn(plantilla("pl2", "GENERICA"));

        assertThrows(RecursoDuplicadoException.class,
                () -> useCase.crearPlantilla(plantilla));
    }

    @Test
    void crearPlantilla_rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> useCase.crearPlantilla(null));

        PlantillaNotificacion sinNombre = plantilla("pl1", "GENERICA");
        sinNombre.setNombre("");
        assertThrows(IllegalArgumentException.class,
                () -> useCase.crearPlantilla(sinNombre));

        PlantillaNotificacion sinAsunto = plantilla("pl1", "GENERICA");
        sinAsunto.setAsunto("");
        assertThrows(IllegalArgumentException.class,
                () -> useCase.crearPlantilla(sinAsunto));

        PlantillaNotificacion sinContenido = plantilla("pl1", "GENERICA");
        sinContenido.setContenido("");
        assertThrows(IllegalArgumentException.class,
                () -> useCase.crearPlantilla(sinContenido));
    }

    @Test
    void actualizarPlantilla_exitosa() {
        PlantillaNotificacion existente = plantilla("pl1", "GENERICA");
        PlantillaNotificacion actualizada = plantilla("pl1", "PRESTAMO_CONFIRMADO");
        actualizada.setEstado("INACTIVA");
        when(plantillaGateway.buscarPlantillaPorId("pl1")).thenReturn(existente);
        when(plantillaGateway.buscarPlantillaPorNombre(actualizada.getNombre()))
                .thenReturn(existente);
        when(plantillaGateway.actualizarPlantilla(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlantillaNotificacion resultado = useCase.actualizarPlantilla("pl1", actualizada);

        assertEquals("pl1", resultado.getIdPlantilla());
        assertEquals("PRESTAMO_CONFIRMADO", resultado.getTipo());
        assertEquals("INACTIVA", resultado.getEstado());
    }

    @Test
    void actualizarPlantilla_rechazaNoEncontradaYDuplicada() {
        when(plantillaGateway.buscarPlantillaPorId("pl1")).thenReturn(null);

        assertThrows(PlantillaNoEncontradaException.class,
                () -> useCase.actualizarPlantilla("pl1", plantilla("pl1", "GENERICA")));

        PlantillaNotificacion existente = plantilla("pl1", "GENERICA");
        PlantillaNotificacion conNombreDuplicado = plantilla("pl1", "GENERICA");
        when(plantillaGateway.buscarPlantillaPorId("pl1")).thenReturn(existente);
        when(plantillaGateway.buscarPlantillaPorNombre(conNombreDuplicado.getNombre()))
                .thenReturn(plantilla("pl2", "GENERICA"));

        assertThrows(RecursoDuplicadoException.class,
                () -> useCase.actualizarPlantilla("pl1", conNombreDuplicado));
    }

    @Test
    void buscarYListarPlantillas() {
        PlantillaNotificacion plantilla = plantilla("pl1", "GENERICA");
        when(plantillaGateway.buscarPlantillaPorId("pl1")).thenReturn(plantilla);
        when(plantillaGateway.listarPlantillas()).thenReturn(List.of(plantilla));

        assertEquals(plantilla, useCase.buscarPlantilla("pl1"));
        assertEquals(1, useCase.listarPlantillas().size());
        assertThrows(PlantillaNoEncontradaException.class,
                () -> useCase.buscarPlantilla("pl2"));
    }

    @Test
    void eliminarPlantilla_validaExistencia() {
        when(plantillaGateway.buscarPlantillaPorId("pl1"))
                .thenReturn(plantilla("pl1", "GENERICA"));

        useCase.eliminarPlantilla("pl1");

        verify(plantillaGateway).eliminarPlantilla("pl1");

        when(plantillaGateway.buscarPlantillaPorId("pl2")).thenReturn(null);
        assertThrows(PlantillaNoEncontradaException.class,
                () -> useCase.eliminarPlantilla("pl2"));
    }

    private PlantillaNotificacion plantilla(String id, String tipo) {
        PlantillaNotificacion plantilla = TestFixtures.plantilla(id, tipo);
        plantilla.setIdPlantilla(id);
        return plantilla;
    }
}
