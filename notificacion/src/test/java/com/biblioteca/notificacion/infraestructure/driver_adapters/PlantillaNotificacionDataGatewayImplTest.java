package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.model.EstadoPlantilla;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.infraestructure.mapper.PlantillaNotificacionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantillaNotificacionDataGatewayImplTest {

    @Mock
    private PlantillaNotificacionDataJpaRepository plantillaDataJpaRepository;

    @Mock
    private PlantillaNotificacionMapper plantillaMapper;

    @InjectMocks
    private PlantillaNotificacionDataGatewayImpl plantillaGateway;

    @Test
    void guardarPlantilla_debeMapearGuardarYRetornarPlantilla() {
        // Arrange
        PlantillaNotificacion plantilla = mock(PlantillaNotificacion.class);
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacionData plantillaDataGuardada =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantillaConvertida =
                mock(PlantillaNotificacion.class);

        when(plantillaMapper.toPlantillaData(plantilla)).thenReturn(plantillaData);
        when(plantillaDataJpaRepository.save(plantillaData))
                .thenReturn(plantillaDataGuardada);
        when(plantillaMapper.toPlantilla(plantillaDataGuardada))
                .thenReturn(plantillaConvertida);

        // Act
        PlantillaNotificacion resultado = plantillaGateway.guardarPlantilla(plantilla);

        // Assert
        assertSame(plantillaConvertida, resultado);
        verify(plantillaMapper).toPlantillaData(plantilla);
        verify(plantillaDataJpaRepository).save(plantillaData);
        verify(plantillaMapper).toPlantilla(plantillaDataGuardada);
    }

    @Test
    void actualizarPlantilla_debeGuardarYRetornarPlantillaActualizada() {
        // Arrange
        PlantillaNotificacion plantilla = mock(PlantillaNotificacion.class);
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacionData plantillaDataActualizada =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantillaActualizada =
                mock(PlantillaNotificacion.class);

        when(plantillaMapper.toPlantillaData(plantilla)).thenReturn(plantillaData);
        when(plantillaDataJpaRepository.save(plantillaData))
                .thenReturn(plantillaDataActualizada);
        when(plantillaMapper.toPlantilla(plantillaDataActualizada))
                .thenReturn(plantillaActualizada);

        // Act
        PlantillaNotificacion resultado =
                plantillaGateway.actualizarPlantilla(plantilla);

        // Assert
        assertSame(plantillaActualizada, resultado);
        verify(plantillaMapper).toPlantillaData(plantilla);
        verify(plantillaDataJpaRepository).save(plantillaData);
        verify(plantillaMapper).toPlantilla(plantillaDataActualizada);
    }

    @Test
    void buscarPlantillaPorId_cuandoExiste_debeRetornarPlantillaMapeada() {
        // Arrange
        String id = "plantilla-1";
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findById(id))
                .thenReturn(Optional.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);

        // Act
        PlantillaNotificacion resultado = plantillaGateway.buscarPlantillaPorId(id);

        // Assert
        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository).findById(id);
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarPlantillaPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String id = "plantilla-inexistente";

        when(plantillaDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        PlantillaNotificacion resultado = plantillaGateway.buscarPlantillaPorId(id);

        // Assert
        assertNull(resultado);
        verify(plantillaDataJpaRepository).findById(id);
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void buscarPlantillaPorNombre_cuandoExiste_debeRetornarPlantillaMapeada() {
        // Arrange
        String nombre = "Confirmacion de prestamo";
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);

        // Act
        PlantillaNotificacion resultado =
                plantillaGateway.buscarPlantillaPorNombre(nombre);

        // Assert
        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarPlantillaPorNombre_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String nombre = "Plantilla inexistente";

        when(plantillaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.empty());

        // Act
        PlantillaNotificacion resultado =
                plantillaGateway.buscarPlantillaPorNombre(nombre);

        // Assert
        assertNull(resultado);
        verify(plantillaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void buscarActivaPorTipo_cuandoExiste_debeRetornarPlantillaMapeada() {
        // Arrange
        String tipo = "PRESTAMO_CONFIRMADO";
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name()))
                .thenReturn(Optional.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);

        // Act
        PlantillaNotificacion resultado = plantillaGateway.buscarActivaPorTipo(tipo);

        // Assert
        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository)
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name());
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarActivaPorTipo_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String tipo = "PRESTAMO_CONFIRMADO";

        when(plantillaDataJpaRepository
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name()))
                .thenReturn(Optional.empty());

        // Act
        PlantillaNotificacion resultado = plantillaGateway.buscarActivaPorTipo(tipo);

        // Assert
        assertNull(resultado);
        verify(plantillaDataJpaRepository)
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name());
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void listarPlantillas_debeMapearYRetornarLaLista() {
        // Arrange
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);

        // Act
        List<PlantillaNotificacion> resultado = plantillaGateway.listarPlantillas();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(plantilla, resultado.get(0));
        verify(plantillaDataJpaRepository).findAllByOrderByNombreAsc();
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void eliminarPlantilla_debeSolicitarEliminacionPorId() {
        // Arrange
        String id = "plantilla-1";

        // Act
        plantillaGateway.eliminarPlantilla(id);

        // Assert
        verify(plantillaDataJpaRepository).deleteById(id);
        verifyNoInteractions(plantillaMapper);
    }
}