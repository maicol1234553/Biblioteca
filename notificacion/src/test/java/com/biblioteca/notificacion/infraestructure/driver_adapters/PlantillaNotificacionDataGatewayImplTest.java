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


        PlantillaNotificacion resultado = plantillaGateway.guardarPlantilla(plantilla);


        assertSame(plantillaConvertida, resultado);
        verify(plantillaMapper).toPlantillaData(plantilla);
        verify(plantillaDataJpaRepository).save(plantillaData);
        verify(plantillaMapper).toPlantilla(plantillaDataGuardada);
    }

    @Test
    void actualizarPlantilla_debeGuardarYRetornarPlantillaActualizada() {

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


        PlantillaNotificacion resultado =
                plantillaGateway.actualizarPlantilla(plantilla);


        assertSame(plantillaActualizada, resultado);
        verify(plantillaMapper).toPlantillaData(plantilla);
        verify(plantillaDataJpaRepository).save(plantillaData);
        verify(plantillaMapper).toPlantilla(plantillaDataActualizada);
    }

    @Test
    void buscarPlantillaPorId_cuandoExiste_debeRetornarPlantillaMapeada() {

        String id = "plantilla-1";
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findById(id))
                .thenReturn(Optional.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);


        PlantillaNotificacion resultado = plantillaGateway.buscarPlantillaPorId(id);


        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository).findById(id);
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarPlantillaPorId_cuandoNoExiste_debeRetornarNull() {

        String id = "plantilla-inexistente";

        when(plantillaDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());


        PlantillaNotificacion resultado = plantillaGateway.buscarPlantillaPorId(id);


        assertNull(resultado);
        verify(plantillaDataJpaRepository).findById(id);
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void buscarPlantillaPorNombre_cuandoExiste_debeRetornarPlantillaMapeada() {

        String nombre = "Confirmacion de prestamo";
        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);


        PlantillaNotificacion resultado =
                plantillaGateway.buscarPlantillaPorNombre(nombre);


        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarPlantillaPorNombre_cuandoNoExiste_debeRetornarNull() {

        String nombre = "Plantilla inexistente";

        when(plantillaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.empty());


        PlantillaNotificacion resultado =
                plantillaGateway.buscarPlantillaPorNombre(nombre);


        assertNull(resultado);
        verify(plantillaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void buscarActivaPorTipo_cuandoExiste_debeRetornarPlantillaMapeada() {

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


        PlantillaNotificacion resultado = plantillaGateway.buscarActivaPorTipo(tipo);


        assertSame(plantilla, resultado);
        verify(plantillaDataJpaRepository)
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name());
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void buscarActivaPorTipo_cuandoNoExiste_debeRetornarNull() {

        String tipo = "PRESTAMO_CONFIRMADO";

        when(plantillaDataJpaRepository
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name()))
                .thenReturn(Optional.empty());


        PlantillaNotificacion resultado = plantillaGateway.buscarActivaPorTipo(tipo);


        assertNull(resultado);
        verify(plantillaDataJpaRepository)
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name());
        verifyNoInteractions(plantillaMapper);
    }

    @Test
    void listarPlantillas_debeMapearYRetornarLaLista() {

        PlantillaNotificacionData plantillaData =
                mock(PlantillaNotificacionData.class);
        PlantillaNotificacion plantilla =
                mock(PlantillaNotificacion.class);

        when(plantillaDataJpaRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(plantillaData));
        when(plantillaMapper.toPlantilla(plantillaData)).thenReturn(plantilla);


        List<PlantillaNotificacion> resultado = plantillaGateway.listarPlantillas();


        assertEquals(1, resultado.size());
        assertSame(plantilla, resultado.get(0));
        verify(plantillaDataJpaRepository).findAllByOrderByNombreAsc();
        verify(plantillaMapper).toPlantilla(plantillaData);
    }

    @Test
    void eliminarPlantilla_debeSolicitarEliminacionPorId() {

        String id = "plantilla-1";


        plantillaGateway.eliminarPlantilla(id);


        verify(plantillaDataJpaRepository).deleteById(id);
        verifyNoInteractions(plantillaMapper);
    }
}