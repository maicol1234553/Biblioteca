package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoPrestamo;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.infraestructure.mapper.PrestamoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoDataGatewayImplTest {

    @Mock
    private PrestamoDataJpaRepository prestamoDataJpaRepository;

    @Mock
    private PrestamoMapper prestamoMapper;

    @InjectMocks
    private PrestamoDataGatewayImpl prestamoDataGateway;

    @Test
    void guardarPrestamo_debeMapearGuardarYRetornarPrestamo() {
        // Arrange
        Prestamo prestamo = mock(Prestamo.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        PrestamoData prestamoDataGuardado = mock(PrestamoData.class);
        Prestamo prestamoConvertido = mock(Prestamo.class);

        when(prestamoMapper.toPrestamoData(prestamo)).thenReturn(prestamoData);
        when(prestamoDataJpaRepository.save(prestamoData)).thenReturn(prestamoDataGuardado);
        when(prestamoMapper.toPrestamo(prestamoDataGuardado)).thenReturn(prestamoConvertido);

        // Act
        Prestamo resultado = prestamoDataGateway.guardarPrestamo(prestamo);

        // Assert
        assertSame(prestamoConvertido, resultado);
        verify(prestamoMapper).toPrestamoData(prestamo);
        verify(prestamoDataJpaRepository).save(prestamoData);
        verify(prestamoMapper).toPrestamo(prestamoDataGuardado);
    }

    @Test
    void actualizarPrestamo_debeGuardarYRetornarPrestamoActualizado() {
        // Arrange
        Prestamo prestamo = mock(Prestamo.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        PrestamoData prestamoDataActualizado = mock(PrestamoData.class);
        Prestamo prestamoActualizado = mock(Prestamo.class);

        when(prestamoMapper.toPrestamoData(prestamo)).thenReturn(prestamoData);
        when(prestamoDataJpaRepository.save(prestamoData))
                .thenReturn(prestamoDataActualizado);
        when(prestamoMapper.toPrestamo(prestamoDataActualizado))
                .thenReturn(prestamoActualizado);

        // Act
        Prestamo resultado = prestamoDataGateway.actualizarPrestamo(prestamo);

        // Assert
        assertSame(prestamoActualizado, resultado);
        verify(prestamoMapper).toPrestamoData(prestamo);
        verify(prestamoDataJpaRepository).save(prestamoData);
        verify(prestamoMapper).toPrestamo(prestamoDataActualizado);
    }

    @Test
    void buscarPrestamoPorId_cuandoExiste_debeRetornarPrestamoMapeado() {
        // Arrange
        String idPrestamo = "prestamo-1";
        PrestamoData prestamoData = mock(PrestamoData.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoDataJpaRepository.findById(idPrestamo))
                .thenReturn(Optional.of(prestamoData));
        when(prestamoMapper.toPrestamo(prestamoData)).thenReturn(prestamo);

        // Act
        Prestamo resultado = prestamoDataGateway.buscarPrestamoPorId(idPrestamo);

        // Assert
        assertSame(prestamo, resultado);
        verify(prestamoDataJpaRepository).findById(idPrestamo);
        verify(prestamoMapper).toPrestamo(prestamoData);
    }

    @Test
    void buscarPrestamoPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idPrestamo = "prestamo-inexistente";

        when(prestamoDataJpaRepository.findById(idPrestamo))
                .thenReturn(Optional.empty());

        // Act
        Prestamo resultado = prestamoDataGateway.buscarPrestamoPorId(idPrestamo);

        // Assert
        assertNull(resultado);
        verify(prestamoDataJpaRepository).findById(idPrestamo);
        verifyNoInteractions(prestamoMapper);
    }

    @Test
    void listarPrestamos_debeMapearYRetornarLaLista() {
        // Arrange
        PrestamoData prestamoData = mock(PrestamoData.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoDataJpaRepository.findAllByOrderByFechaPrestamoDesc())
                .thenReturn(List.of(prestamoData));
        when(prestamoMapper.toPrestamo(prestamoData)).thenReturn(prestamo);

        // Act
        List<Prestamo> resultado = prestamoDataGateway.listarPrestamos();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(prestamo, resultado.get(0));
        verify(prestamoDataJpaRepository).findAllByOrderByFechaPrestamoDesc();
        verify(prestamoMapper).toPrestamo(prestamoData);
    }

    @Test
    void listarPrestamosActivosPorUsuario_debeFiltrarPorEstadosActivos() {
        // Arrange
        String idUsuario = "usuario-1";
        List<String> estadosActivos = List.of(
                EstadoPrestamo.ACTIVO.name(),
                EstadoPrestamo.VENCIDO.name()
        );
        PrestamoData prestamoData = mock(PrestamoData.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoDataJpaRepository
                .findByIdUsuarioAndEstadoInOrderByFechaPrestamoDesc(idUsuario, estadosActivos))
                .thenReturn(List.of(prestamoData));
        when(prestamoMapper.toPrestamo(prestamoData)).thenReturn(prestamo);

        // Act
        List<Prestamo> resultado =
                prestamoDataGateway.listarPrestamosActivosPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(prestamo, resultado.get(0));
        verify(prestamoDataJpaRepository)
                .findByIdUsuarioAndEstadoInOrderByFechaPrestamoDesc(idUsuario, estadosActivos);
        verify(prestamoMapper).toPrestamo(prestamoData);
    }

    @Test
    void listarHistorialPorUsuario_debeBuscarYMapearPrestamos() {
        // Arrange
        String idUsuario = "usuario-1";
        PrestamoData prestamoData = mock(PrestamoData.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoDataJpaRepository.findByIdUsuarioOrderByFechaPrestamoDesc(idUsuario))
                .thenReturn(List.of(prestamoData));
        when(prestamoMapper.toPrestamo(prestamoData)).thenReturn(prestamo);

        // Act
        List<Prestamo> resultado =
                prestamoDataGateway.listarHistorialPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(prestamo, resultado.get(0));
        verify(prestamoDataJpaRepository)
                .findByIdUsuarioOrderByFechaPrestamoDesc(idUsuario);
        verify(prestamoMapper).toPrestamo(prestamoData);
    }

    @Test
    void contarPrestamosActivosPorUsuario_debeRetornarCantidad() {
        // Arrange
        String idUsuario = "usuario-1";
        List<String> estadosActivos = List.of(
                EstadoPrestamo.ACTIVO.name(),
                EstadoPrestamo.VENCIDO.name()
        );

        when(prestamoDataJpaRepository.countByIdUsuarioAndEstadoIn(idUsuario, estadosActivos))
                .thenReturn(2L);

        // Act
        long resultado = prestamoDataGateway.contarPrestamosActivosPorUsuario(idUsuario);

        // Assert
        assertEquals(2L, resultado);
        verify(prestamoDataJpaRepository)
                .countByIdUsuarioAndEstadoIn(idUsuario, estadosActivos);
        verifyNoInteractions(prestamoMapper);
    }

    @Test
    void existePrestamoActivoPorEjemplar_cuandoExiste_debeRetornarTrue() {
        // Arrange
        String idEjemplar = "ejemplar-1";
        List<String> estadosActivos = List.of(
                EstadoPrestamo.ACTIVO.name(),
                EstadoPrestamo.VENCIDO.name()
        );

        when(prestamoDataJpaRepository
                .existsByIdEjemplarAndEstadoIn(idEjemplar, estadosActivos))
                .thenReturn(true);

        // Act
        boolean resultado =
                prestamoDataGateway.existePrestamoActivoPorEjemplar(idEjemplar);

        // Assert
        assertTrue(resultado);
        verify(prestamoDataJpaRepository)
                .existsByIdEjemplarAndEstadoIn(idEjemplar, estadosActivos);
        verifyNoInteractions(prestamoMapper);
    }

    @Test
    void existePrestamoActivoPorEjemplar_cuandoNoExiste_debeRetornarFalse() {
        // Arrange
        String idEjemplar = "ejemplar-1";
        List<String> estadosActivos = List.of(
                EstadoPrestamo.ACTIVO.name(),
                EstadoPrestamo.VENCIDO.name()
        );

        when(prestamoDataJpaRepository
                .existsByIdEjemplarAndEstadoIn(idEjemplar, estadosActivos))
                .thenReturn(false);

        // Act
        boolean resultado =
                prestamoDataGateway.existePrestamoActivoPorEjemplar(idEjemplar);

        // Assert
        assertFalse(resultado);
        verify(prestamoDataJpaRepository)
                .existsByIdEjemplarAndEstadoIn(idEjemplar, estadosActivos);
        verifyNoInteractions(prestamoMapper);
    }

    @Test
    void listarPrestamosVencidos_debeBuscarPorEstadoYFechaYMapearResultados() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 12, 0);
        PrestamoData prestamoData = mock(PrestamoData.class);
        Prestamo prestamo = mock(Prestamo.class);

        when(prestamoDataJpaRepository
                .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                        EstadoPrestamo.ACTIVO.name(), fecha))
                .thenReturn(List.of(prestamoData));
        when(prestamoMapper.toPrestamo(prestamoData)).thenReturn(prestamo);

        // Act
        List<Prestamo> resultado = prestamoDataGateway.listarPrestamosVencidos(fecha);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(prestamo, resultado.get(0));
        verify(prestamoDataJpaRepository)
                .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                        EstadoPrestamo.ACTIVO.name(), fecha);
        verify(prestamoMapper).toPrestamo(prestamoData);
    }

    @Test
    void eliminarPrestamo_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idPrestamo = "prestamo-1";

        // Act
        prestamoDataGateway.eliminarPrestamo(idPrestamo);

        // Assert
        verify(prestamoDataJpaRepository).deleteById(idPrestamo);
        verify(prestamoDataJpaRepository).flush();
        verifyNoInteractions(prestamoMapper);
    }

    @Test
    void eliminarPrestamo_cuandoTieneInformacionAsociada_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idPrestamo = "prestamo-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(prestamoDataJpaRepository).flush();

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> prestamoDataGateway.eliminarPrestamo(idPrestamo)
        );

        // Assert
        assertEquals(
                "No se puede eliminar el prestamo porque tiene devoluciones, multas o renovaciones asociadas",
                excepcion.getMessage()
        );
        verify(prestamoDataJpaRepository).deleteById(idPrestamo);
        verify(prestamoDataJpaRepository).flush();
    }
}