package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoMulta;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.infraestructure.mapper.MultaMapper;
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
class MultaDataGatewayImplTest {

    @Mock
    private MultaDataJpaRepository multaDataJpaRepository;

    @Mock
    private PrestamoDataJpaRepository prestamoDataJpaRepository;

    @Mock
    private MultaMapper multaMapper;

    @InjectMocks
    private MultaDataGatewayImpl multaDataGateway;

    @Test
    void guardarMulta_debeBuscarPrestamoMapearGuardarYRetornarMulta() {
        // Arrange
        Multa multa = mock(Multa.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        MultaData multaData = mock(MultaData.class);
        MultaData multaDataGuardada = mock(MultaData.class);
        Multa multaConvertida = mock(Multa.class);

        when(multa.getIdPrestamo()).thenReturn("prestamo-1");
        when(prestamoDataJpaRepository.findById("prestamo-1"))
                .thenReturn(Optional.of(prestamoData));
        when(multaMapper.toMultaData(multa, prestamoData)).thenReturn(multaData);
        when(multaDataJpaRepository.save(multaData)).thenReturn(multaDataGuardada);
        when(multaMapper.toMulta(multaDataGuardada)).thenReturn(multaConvertida);

        // Act
        Multa resultado = multaDataGateway.guardarMulta(multa);

        // Assert
        assertSame(multaConvertida, resultado);
        verify(prestamoDataJpaRepository).findById("prestamo-1");
        verify(multaMapper).toMultaData(multa, prestamoData);
        verify(multaDataJpaRepository).save(multaData);
        verify(multaMapper).toMulta(multaDataGuardada);
    }

    @Test
    void actualizarMulta_debeBuscarPrestamoGuardarYRetornarMultaActualizada() {
        // Arrange
        Multa multa = mock(Multa.class);
        PrestamoData prestamoData = mock(PrestamoData.class);
        MultaData multaData = mock(MultaData.class);
        MultaData multaDataActualizada = mock(MultaData.class);
        Multa multaActualizada = mock(Multa.class);

        when(multa.getIdPrestamo()).thenReturn("prestamo-1");
        when(prestamoDataJpaRepository.findById("prestamo-1"))
                .thenReturn(Optional.of(prestamoData));
        when(multaMapper.toMultaData(multa, prestamoData)).thenReturn(multaData);
        when(multaDataJpaRepository.save(multaData)).thenReturn(multaDataActualizada);
        when(multaMapper.toMulta(multaDataActualizada)).thenReturn(multaActualizada);

        // Act
        Multa resultado = multaDataGateway.actualizarMulta(multa);

        // Assert
        assertSame(multaActualizada, resultado);
        verify(prestamoDataJpaRepository).findById("prestamo-1");
        verify(multaMapper).toMultaData(multa, prestamoData);
        verify(multaDataJpaRepository).save(multaData);
        verify(multaMapper).toMulta(multaDataActualizada);
    }

    @Test
    void guardarMulta_cuandoPrestamoNoExiste_debeLanzarExcepcion() {
        // Arrange
        Multa multa = mock(Multa.class);

        when(multa.getIdPrestamo()).thenReturn("prestamo-inexistente");
        when(prestamoDataJpaRepository.findById("prestamo-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        PrestamoNoEncontradoException excepcion = assertThrows(
                PrestamoNoEncontradoException.class,
                () -> multaDataGateway.guardarMulta(multa)
        );

        // Assert
        assertEquals(
                "Prestamo no encontrado con id: prestamo-inexistente",
                excepcion.getMessage()
        );
        verify(multaDataJpaRepository, never()).save(any());
        verifyNoInteractions(multaMapper);
    }

    @Test
    void buscarMultaPorId_cuandoExiste_debeRetornarMultaMapeada() {
        // Arrange
        String idMulta = "multa-1";
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository.findById(idMulta))
                .thenReturn(Optional.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        Multa resultado = multaDataGateway.buscarMultaPorId(idMulta);

        // Assert
        assertSame(multa, resultado);
        verify(multaDataJpaRepository).findById(idMulta);
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void buscarMultaPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idMulta = "multa-inexistente";

        when(multaDataJpaRepository.findById(idMulta))
                .thenReturn(Optional.empty());

        // Act
        Multa resultado = multaDataGateway.buscarMultaPorId(idMulta);

        // Assert
        assertNull(resultado);
        verify(multaDataJpaRepository).findById(idMulta);
        verifyNoInteractions(multaMapper);
    }

    @Test
    void listarMultas_debeMapearYRetornarLaLista() {
        // Arrange
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository.findAllByOrderByFechaGeneracionDesc())
                .thenReturn(List.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        List<Multa> resultado = multaDataGateway.listarMultas();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(multa, resultado.get(0));
        verify(multaDataJpaRepository).findAllByOrderByFechaGeneracionDesc();
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void listarMultasPorPrestamo_debeBuscarYMapearLasMultas() {
        // Arrange
        String idPrestamo = "prestamo-1";
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository
                .findByPrestamo_IdPrestamoOrderByFechaGeneracionDesc(idPrestamo))
                .thenReturn(List.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        List<Multa> resultado = multaDataGateway.listarMultasPorPrestamo(idPrestamo);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(multa, resultado.get(0));
        verify(multaDataJpaRepository)
                .findByPrestamo_IdPrestamoOrderByFechaGeneracionDesc(idPrestamo);
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void listarMultasPorUsuario_debeBuscarYMapearLasMultas() {
        // Arrange
        String idUsuario = "usuario-1";
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository
                .findByPrestamo_IdUsuarioOrderByFechaGeneracionDesc(idUsuario))
                .thenReturn(List.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        List<Multa> resultado = multaDataGateway.listarMultasPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(multa, resultado.get(0));
        verify(multaDataJpaRepository)
                .findByPrestamo_IdUsuarioOrderByFechaGeneracionDesc(idUsuario);
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void listarMultasPendientesPorUsuario_debeFiltrarPorEstadoPendiente() {
        // Arrange
        String idUsuario = "usuario-1";
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository
                .findByPrestamo_IdUsuarioAndEstadoOrderByFechaGeneracionDesc(
                        idUsuario, EstadoMulta.PENDIENTE.name()))
                .thenReturn(List.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        List<Multa> resultado =
                multaDataGateway.listarMultasPendientesPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(multa, resultado.get(0));
        verify(multaDataJpaRepository)
                .findByPrestamo_IdUsuarioAndEstadoOrderByFechaGeneracionDesc(
                        idUsuario, EstadoMulta.PENDIENTE.name());
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void buscarMultaPendientePorPrestamo_cuandoExiste_debeRetornarMultaMapeada() {
        // Arrange
        String idPrestamo = "prestamo-1";
        MultaData multaData = mock(MultaData.class);
        Multa multa = mock(Multa.class);

        when(multaDataJpaRepository
                .findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
                        idPrestamo, EstadoMulta.PENDIENTE.name()))
                .thenReturn(Optional.of(multaData));
        when(multaMapper.toMulta(multaData)).thenReturn(multa);

        // Act
        Multa resultado =
                multaDataGateway.buscarMultaPendientePorPrestamo(idPrestamo);

        // Assert
        assertSame(multa, resultado);
        verify(multaDataJpaRepository)
                .findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
                        idPrestamo, EstadoMulta.PENDIENTE.name());
        verify(multaMapper).toMulta(multaData);
    }

    @Test
    void buscarMultaPendientePorPrestamo_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idPrestamo = "prestamo-sin-multa-pendiente";

        when(multaDataJpaRepository
                .findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
                        idPrestamo, EstadoMulta.PENDIENTE.name()))
                .thenReturn(Optional.empty());

        // Act
        Multa resultado =
                multaDataGateway.buscarMultaPendientePorPrestamo(idPrestamo);

        // Assert
        assertNull(resultado);
        verify(multaDataJpaRepository)
                .findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
                        idPrestamo, EstadoMulta.PENDIENTE.name());
        verifyNoInteractions(multaMapper);
    }

    @Test
    void eliminarMulta_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idMulta = "multa-1";

        // Act
        multaDataGateway.eliminarMulta(idMulta);

        // Assert
        verify(multaDataJpaRepository).deleteById(idMulta);
        verify(multaDataJpaRepository).flush();
        verifyNoInteractions(multaMapper, prestamoDataJpaRepository);
    }

    @Test
    void eliminarMulta_cuandoTieneInformacionAsociada_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idMulta = "multa-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(multaDataJpaRepository).flush();

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> multaDataGateway.eliminarMulta(idMulta)
        );

        // Assert
        assertEquals(
                "No se puede eliminar la multa porque tiene informacion asociada",
                excepcion.getMessage()
        );
        verify(multaDataJpaRepository).deleteById(idMulta);
        verify(multaDataJpaRepository).flush();
    }
}