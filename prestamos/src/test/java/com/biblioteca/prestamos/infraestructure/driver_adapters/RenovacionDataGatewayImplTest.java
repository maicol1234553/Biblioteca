package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.infraestructure.mapper.RenovacionMapper;
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
class RenovacionDataGatewayImplTest {

    @Mock
    private RenovacionDataJpaRepository renovacionDataJpaRepository;

    @Mock
    private PrestamoDataJpaRepository prestamoDataJpaRepository;

    @Mock
    private RenovacionMapper renovacionMapper;

    @InjectMocks
    private RenovacionDataGatewayImpl renovacionDataGateway;

    @Test
    void guardarRenovacion_debeBuscarPrestamoMapearGuardarYRetornarRenovacion() {
        // Arrange
        Renovacion renovacion = mock(Renovacion.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        RenovacionData renovacionData = mock(RenovacionData.class);
        RenovacionData renovacionDataGuardada = mock(RenovacionData.class);
        Renovacion renovacionConvertida = mock(Renovacion.class);

        when(renovacion.getIdPrestamo()).thenReturn("prestamo-1");
        when(prestamoDataJpaRepository.findById("prestamo-1"))
                .thenReturn(Optional.of(prestamoData));
        when(renovacionMapper.toRenovacionData(renovacion, prestamoData))
                .thenReturn(renovacionData);
        when(renovacionDataJpaRepository.save(renovacionData))
                .thenReturn(renovacionDataGuardada);
        when(renovacionMapper.toRenovacion(renovacionDataGuardada))
                .thenReturn(renovacionConvertida);

        // Act
        Renovacion resultado = renovacionDataGateway.guardarRenovacion(renovacion);

        // Assert
        assertSame(renovacionConvertida, resultado);
        verify(prestamoDataJpaRepository).findById("prestamo-1");
        verify(renovacionMapper).toRenovacionData(renovacion, prestamoData);
        verify(renovacionDataJpaRepository).save(renovacionData);
        verify(renovacionMapper).toRenovacion(renovacionDataGuardada);
    }

    @Test
    void guardarRenovacion_cuandoPrestamoNoExiste_debeLanzarExcepcion() {
        // Arrange
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacion.getIdPrestamo()).thenReturn("prestamo-inexistente");
        when(prestamoDataJpaRepository.findById("prestamo-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        PrestamoNoEncontradoException excepcion = assertThrows(
                PrestamoNoEncontradoException.class,
                () -> renovacionDataGateway.guardarRenovacion(renovacion)
        );

        // Assert
        assertEquals(
                "Prestamo no encontrado con id: prestamo-inexistente",
                excepcion.getMessage()
        );
        verify(renovacionDataJpaRepository, never()).save(any());
        verifyNoInteractions(renovacionMapper);
    }

    @Test
    void buscarRenovacionPorId_cuandoExiste_debeRetornarRenovacionMapeada() {
        // Arrange
        String idRenovacion = "renovacion-1";
        RenovacionData renovacionData = mock(RenovacionData.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionDataJpaRepository.findById(idRenovacion))
                .thenReturn(Optional.of(renovacionData));
        when(renovacionMapper.toRenovacion(renovacionData)).thenReturn(renovacion);

        // Act
        Renovacion resultado =
                renovacionDataGateway.buscarRenovacionPorId(idRenovacion);

        // Assert
        assertSame(renovacion, resultado);
        verify(renovacionDataJpaRepository).findById(idRenovacion);
        verify(renovacionMapper).toRenovacion(renovacionData);
    }

    @Test
    void buscarRenovacionPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idRenovacion = "renovacion-inexistente";

        when(renovacionDataJpaRepository.findById(idRenovacion))
                .thenReturn(Optional.empty());

        // Act
        Renovacion resultado =
                renovacionDataGateway.buscarRenovacionPorId(idRenovacion);

        // Assert
        assertNull(resultado);
        verify(renovacionDataJpaRepository).findById(idRenovacion);
        verifyNoInteractions(renovacionMapper);
    }

    @Test
    void listarRenovaciones_debeMapearYRetornarLaLista() {
        // Arrange
        RenovacionData renovacionData = mock(RenovacionData.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionDataJpaRepository.findAllByOrderByFechaSolicitudDesc())
                .thenReturn(List.of(renovacionData));
        when(renovacionMapper.toRenovacion(renovacionData)).thenReturn(renovacion);

        // Act
        List<Renovacion> resultado = renovacionDataGateway.listarRenovaciones();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(renovacion, resultado.get(0));
        verify(renovacionDataJpaRepository).findAllByOrderByFechaSolicitudDesc();
        verify(renovacionMapper).toRenovacion(renovacionData);
    }

    @Test
    void listarRenovacionesPorPrestamo_debeBuscarYMapearLasRenovaciones() {
        // Arrange
        String idPrestamo = "prestamo-1";
        RenovacionData renovacionData = mock(RenovacionData.class);
        Renovacion renovacion = mock(Renovacion.class);

        when(renovacionDataJpaRepository
                .findByPrestamo_IdPrestamoOrderByFechaSolicitudDesc(idPrestamo))
                .thenReturn(List.of(renovacionData));
        when(renovacionMapper.toRenovacion(renovacionData)).thenReturn(renovacion);

        // Act
        List<Renovacion> resultado =
                renovacionDataGateway.listarRenovacionesPorPrestamo(idPrestamo);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(renovacion, resultado.get(0));
        verify(renovacionDataJpaRepository)
                .findByPrestamo_IdPrestamoOrderByFechaSolicitudDesc(idPrestamo);
        verify(renovacionMapper).toRenovacion(renovacionData);
    }

    @Test
    void contarRenovacionesPorPrestamo_debeRetornarLaCantidad() {
        // Arrange
        String idPrestamo = "prestamo-1";

        when(renovacionDataJpaRepository.countByPrestamo_IdPrestamo(idPrestamo))
                .thenReturn(2L);

        // Act
        long resultado =
                renovacionDataGateway.contarRenovacionesPorPrestamo(idPrestamo);

        // Assert
        assertEquals(2L, resultado);
        verify(renovacionDataJpaRepository).countByPrestamo_IdPrestamo(idPrestamo);
        verifyNoInteractions(renovacionMapper);
    }

    @Test
    void eliminarRenovacion_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idRenovacion = "renovacion-1";

        // Act
        renovacionDataGateway.eliminarRenovacion(idRenovacion);

        // Assert
        verify(renovacionDataJpaRepository).deleteById(idRenovacion);
        verify(renovacionDataJpaRepository).flush();
        verifyNoInteractions(renovacionMapper, prestamoDataJpaRepository);
    }

    @Test
    void eliminarRenovacion_cuandoTieneInformacionAsociada_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idRenovacion = "renovacion-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(renovacionDataJpaRepository).flush();

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> renovacionDataGateway.eliminarRenovacion(idRenovacion)
        );

        // Assert
        assertEquals(
                "No se puede eliminar la renovacion porque tiene informacion asociada",
                excepcion.getMessage()
        );
        verify(renovacionDataJpaRepository).deleteById(idRenovacion);
        verify(renovacionDataJpaRepository).flush();
    }
}