package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.infraestructure.mapper.DevolucionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevolucionDataGatewayImplTest {

    @Mock
    private DevolucionDataJpaRepository devolucionDataJpaRepository;

    @Mock
    private PrestamoDataJpaRepository prestamoDataJpaRepository;

    @Mock
    private DevolucionMapper devolucionMapper;

    @InjectMocks
    private DevolucionDataGatewayImpl devolucionDataGateway;

    @Test
    void guardarDevolucion_debeBuscarPrestamoMapearGuardarYRetornarDevolucion() {

        Devolucion devolucion = mock(Devolucion.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        DevolucionData devolucionData = mock(DevolucionData.class);
        DevolucionData devolucionDataGuardada = mock(DevolucionData.class);
        Devolucion devolucionConvertida = mock(Devolucion.class);

        when(devolucion.getIdPrestamo()).thenReturn("prestamo-1");
        when(prestamoDataJpaRepository.findById("prestamo-1"))
                .thenReturn(Optional.of(prestamoData));
        when(devolucionMapper.toDevolucionData(devolucion, prestamoData))
                .thenReturn(devolucionData);
        when(devolucionDataJpaRepository.save(devolucionData))
                .thenReturn(devolucionDataGuardada);
        when(devolucionMapper.toDevolucion(devolucionDataGuardada))
                .thenReturn(devolucionConvertida);


        Devolucion resultado = devolucionDataGateway.guardarDevolucion(devolucion);


        assertSame(devolucionConvertida, resultado);
        verify(prestamoDataJpaRepository).findById("prestamo-1");
        verify(devolucionMapper).toDevolucionData(devolucion, prestamoData);
        verify(devolucionDataJpaRepository).save(devolucionData);
        verify(devolucionMapper).toDevolucion(devolucionDataGuardada);
    }

    @Test
    void guardarDevolucion_cuandoPrestamoNoExiste_debeLanzarExcepcion() {

        Devolucion devolucion = mock(Devolucion.class);

        when(devolucion.getIdPrestamo()).thenReturn("prestamo-inexistente");
        when(prestamoDataJpaRepository.findById("prestamo-inexistente"))
                .thenReturn(Optional.empty());


        PrestamoNoEncontradoException excepcion = assertThrows(
                PrestamoNoEncontradoException.class,
                () -> devolucionDataGateway.guardarDevolucion(devolucion)
        );


        assertEquals(
                "Prestamo no encontrado con id: prestamo-inexistente",
                excepcion.getMessage()
        );
        verify(devolucionDataJpaRepository, never()).save(any());
        verifyNoInteractions(devolucionMapper);
    }

    @Test
    void buscarDevolucionPorId_cuandoExiste_debeRetornarDevolucionMapeada() {

        String id = "devolucion-1";
        DevolucionData devolucionData = mock(DevolucionData.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionDataJpaRepository.findById(id))
                .thenReturn(Optional.of(devolucionData));
        when(devolucionMapper.toDevolucion(devolucionData)).thenReturn(devolucion);


        Devolucion resultado = devolucionDataGateway.buscarDevolucionPorId(id);


        assertSame(devolucion, resultado);
        verify(devolucionDataJpaRepository).findById(id);
        verify(devolucionMapper).toDevolucion(devolucionData);
    }

    @Test
    void buscarDevolucionPorId_cuandoNoExiste_debeRetornarNull() {

        String id = "devolucion-inexistente";

        when(devolucionDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());


        Devolucion resultado = devolucionDataGateway.buscarDevolucionPorId(id);


        assertNull(resultado);
        verify(devolucionDataJpaRepository).findById(id);
        verifyNoInteractions(devolucionMapper);
    }

    @Test
    void buscarDevolucionPorPrestamo_cuandoExiste_debeRetornarDevolucionMapeada() {

        String idPrestamo = "prestamo-1";
        DevolucionData devolucionData = mock(DevolucionData.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionDataJpaRepository.findByPrestamo_IdPrestamo(idPrestamo))
                .thenReturn(Optional.of(devolucionData));
        when(devolucionMapper.toDevolucion(devolucionData)).thenReturn(devolucion);


        Devolucion resultado =
                devolucionDataGateway.buscarDevolucionPorPrestamo(idPrestamo);


        assertSame(devolucion, resultado);
        verify(devolucionDataJpaRepository).findByPrestamo_IdPrestamo(idPrestamo);
        verify(devolucionMapper).toDevolucion(devolucionData);
    }

    @Test
    void buscarDevolucionPorPrestamo_cuandoNoExiste_debeRetornarNull() {

        String idPrestamo = "prestamo-sin-devolucion";

        when(devolucionDataJpaRepository.findByPrestamo_IdPrestamo(idPrestamo))
                .thenReturn(Optional.empty());


        Devolucion resultado =
                devolucionDataGateway.buscarDevolucionPorPrestamo(idPrestamo);


        assertNull(resultado);
        verify(devolucionDataJpaRepository).findByPrestamo_IdPrestamo(idPrestamo);
        verifyNoInteractions(devolucionMapper);
    }

    @Test
    void listarDevoluciones_debeMapearYRetornarLaLista() {

        DevolucionData devolucionData = mock(DevolucionData.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionDataJpaRepository.findAllByOrderByFechaDevolucionDesc())
                .thenReturn(List.of(devolucionData));
        when(devolucionMapper.toDevolucion(devolucionData)).thenReturn(devolucion);


        List<Devolucion> resultado = devolucionDataGateway.listarDevoluciones();


        assertEquals(1, resultado.size());
        assertSame(devolucion, resultado.get(0));
        verify(devolucionDataJpaRepository).findAllByOrderByFechaDevolucionDesc();
        verify(devolucionMapper).toDevolucion(devolucionData);
    }

    @Test
    void listarDevolucionesPorPrestamo_debeMapearYRetornarLaLista() {

        String idPrestamo = "prestamo-1";
        DevolucionData devolucionData = mock(DevolucionData.class);
        Devolucion devolucion = mock(Devolucion.class);

        when(devolucionDataJpaRepository
                .findByPrestamo_IdPrestamoOrderByFechaDevolucionDesc(idPrestamo))
                .thenReturn(List.of(devolucionData));
        when(devolucionMapper.toDevolucion(devolucionData)).thenReturn(devolucion);


        List<Devolucion> resultado =
                devolucionDataGateway.listarDevolucionesPorPrestamo(idPrestamo);


        assertEquals(1, resultado.size());
        assertSame(devolucion, resultado.get(0));
        verify(devolucionDataJpaRepository)
                .findByPrestamo_IdPrestamoOrderByFechaDevolucionDesc(idPrestamo);
        verify(devolucionMapper).toDevolucion(devolucionData);
    }

    @Test
    void eliminarDevolucion_cuandoNoExiste_debeTerminarSinEliminar() {

        String id = "devolucion-inexistente";

        when(devolucionDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());


        devolucionDataGateway.eliminarDevolucion(id);


        verify(devolucionDataJpaRepository).findById(id);
        verify(devolucionDataJpaRepository, never()).delete(any(DevolucionData.class));
        verify(devolucionDataJpaRepository, never()).flush();
        verifyNoInteractions(prestamoDataJpaRepository);
    }

    @Test
    void eliminarDevolucion_cuandoExisteConPrestamo_debeDesvincularYEliminar() {

        String id = "devolucion-1";
        DevolucionData devolucionData = mock(DevolucionData.class);
        PrestamoData prestamoData = mock(PrestamoData.class);

        when(devolucionDataJpaRepository.findById(id))
                .thenReturn(Optional.of(devolucionData));
        when(devolucionData.getPrestamo()).thenReturn(prestamoData);


        devolucionDataGateway.eliminarDevolucion(id);


        verify(prestamoData).setDevolucion(null);
        verify(prestamoDataJpaRepository).save(prestamoData);
        verify(devolucionDataJpaRepository).delete(devolucionData);
        verify(devolucionDataJpaRepository).flush();
    }

    @Test
    void eliminarDevolucion_cuandoTieneErrorDeIntegridad_debeLanzarRecursoEnUsoException() {

        String id = "devolucion-1";
        DevolucionData devolucionData = mock(DevolucionData.class);

        when(devolucionDataJpaRepository.findById(id))
                .thenReturn(Optional.of(devolucionData));
        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(devolucionDataJpaRepository).flush();


        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> devolucionDataGateway.eliminarDevolucion(id)
        );


        assertEquals(
                "No se puede eliminar la devolucion porque tiene informacion asociada",
                excepcion.getMessage()
        );
        verify(devolucionDataJpaRepository).delete(devolucionData);
        verify(devolucionDataJpaRepository).flush();
    }
}