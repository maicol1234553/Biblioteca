package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.infraestructure.driver_adapters.PlantillaNotificacionData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlantillaNotificacionMapperTest {

    private final PlantillaNotificacionMapper mapper =
            new PlantillaNotificacionMapper();

    @Test
    void toPlantilla_cuandoDataEsNull_debeRetornarNull() {
        // Act
        PlantillaNotificacion resultado = mapper.toPlantilla(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toPlantilla_debeCopiarTodosLosCampos() {
        // Arrange
        PlantillaNotificacionData data =
                mock(PlantillaNotificacionData.class);

        when(data.getIdPlantilla()).thenReturn("plantilla-1");
        when(data.getNombre()).thenReturn("Aviso de préstamo");
        when(data.getAsunto()).thenReturn("Préstamo confirmado");
        when(data.getContenido()).thenReturn("Hola, tu préstamo fue confirmado.");
        when(data.getTipo()).thenReturn("PRESTAMO");
        when(data.getEstado()).thenReturn("ACTIVA");

        // Act
        PlantillaNotificacion resultado = mapper.toPlantilla(data);

        // Assert
        assertAll(
                () -> assertEquals("plantilla-1", resultado.getIdPlantilla()),
                () -> assertEquals("Aviso de préstamo", resultado.getNombre()),
                () -> assertEquals(
                        "Préstamo confirmado",
                        resultado.getAsunto()
                ),
                () -> assertEquals(
                        "Hola, tu préstamo fue confirmado.",
                        resultado.getContenido()
                ),
                () -> assertEquals("PRESTAMO", resultado.getTipo()),
                () -> assertEquals("ACTIVA", resultado.getEstado())
        );
    }

    @Test
    void toPlantillaData_debeCopiarTodosLosCampos() {
        // Arrange
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantilla.getIdPlantilla()).thenReturn("plantilla-1");
        when(plantilla.getNombre()).thenReturn("Aviso de préstamo");
        when(plantilla.getAsunto()).thenReturn("Préstamo confirmado");
        when(plantilla.getContenido())
                .thenReturn("Hola, tu préstamo fue confirmado.");
        when(plantilla.getTipo()).thenReturn("PRESTAMO");
        when(plantilla.getEstado()).thenReturn("ACTIVA");

        // Act
        PlantillaNotificacionData resultado =
                mapper.toPlantillaData(plantilla);

        // Assert
        assertAll(
                () -> assertEquals("plantilla-1", resultado.getIdPlantilla()),
                () -> assertEquals("Aviso de préstamo", resultado.getNombre()),
                () -> assertEquals(
                        "Préstamo confirmado",
                        resultado.getAsunto()
                ),
                () -> assertEquals(
                        "Hola, tu préstamo fue confirmado.",
                        resultado.getContenido()
                ),
                () -> assertEquals("PRESTAMO", resultado.getTipo()),
                () -> assertEquals("ACTIVA", resultado.getEstado())
        );
    }

    @Test
    void toPlantillaData_cuandoPlantillaEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toPlantillaData(null)
        );
    }
}
