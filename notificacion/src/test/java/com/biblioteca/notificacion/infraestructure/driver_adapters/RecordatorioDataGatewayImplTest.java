package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.model.EstadoRecordatorio;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.infraestructure.mapper.RecordatorioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioDataGatewayImplTest {

    @Mock
    private RecordatorioDataJpaRepository recordatorioDataJpaRepository;

    @Mock
    private RecordatorioMapper recordatorioMapper;

    @InjectMocks
    private RecordatorioDataGatewayImpl recordatorioDataGateway;

    @Test
    void guardarRecordatorio_debeMapearGuardarYRetornarRecordatorio() {
        // Arrange
        Recordatorio recordatorio = mock(Recordatorio.class);
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        RecordatorioData recordatorioDataGuardado = mock(RecordatorioData.class);
        Recordatorio recordatorioConvertido = mock(Recordatorio.class);

        when(recordatorioMapper.toRecordatorioData(recordatorio))
                .thenReturn(recordatorioData);
        when(recordatorioDataJpaRepository.save(recordatorioData))
                .thenReturn(recordatorioDataGuardado);
        when(recordatorioMapper.toRecordatorio(recordatorioDataGuardado))
                .thenReturn(recordatorioConvertido);

        // Act
        Recordatorio resultado =
                recordatorioDataGateway.guardarRecordatorio(recordatorio);

        // Assert
        assertSame(recordatorioConvertido, resultado);
        verify(recordatorioMapper).toRecordatorioData(recordatorio);
        verify(recordatorioDataJpaRepository).save(recordatorioData);
        verify(recordatorioMapper).toRecordatorio(recordatorioDataGuardado);
    }

    @Test
    void actualizarRecordatorio_debeGuardarYRetornarRecordatorioActualizado() {
        // Arrange
        Recordatorio recordatorio = mock(Recordatorio.class);
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        RecordatorioData recordatorioDataActualizado = mock(RecordatorioData.class);
        Recordatorio recordatorioActualizado = mock(Recordatorio.class);

        when(recordatorioMapper.toRecordatorioData(recordatorio))
                .thenReturn(recordatorioData);
        when(recordatorioDataJpaRepository.save(recordatorioData))
                .thenReturn(recordatorioDataActualizado);
        when(recordatorioMapper.toRecordatorio(recordatorioDataActualizado))
                .thenReturn(recordatorioActualizado);

        // Act
        Recordatorio resultado =
                recordatorioDataGateway.actualizarRecordatorio(recordatorio);

        // Assert
        assertSame(recordatorioActualizado, resultado);
        verify(recordatorioMapper).toRecordatorioData(recordatorio);
        verify(recordatorioDataJpaRepository).save(recordatorioData);
        verify(recordatorioMapper).toRecordatorio(recordatorioDataActualizado);
    }

    @Test
    void buscarRecordatorioPorId_cuandoExiste_debeRetornarRecordatorioMapeado() {
        // Arrange
        String id = "recordatorio-1";
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findById(id))
                .thenReturn(Optional.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);

        // Act
        Recordatorio resultado =
                recordatorioDataGateway.buscarRecordatorioPorId(id);

        // Assert
        assertSame(recordatorio, resultado);
        verify(recordatorioDataJpaRepository).findById(id);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void buscarRecordatorioPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String id = "recordatorio-inexistente";

        when(recordatorioDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());

        // Act
        Recordatorio resultado =
                recordatorioDataGateway.buscarRecordatorioPorId(id);

        // Assert
        assertEquals(null, resultado);
        verify(recordatorioDataJpaRepository).findById(id);
        verifyNoInteractions(recordatorioMapper);
    }

    @Test
    void listarRecordatorios_debeMapearYRetornarLaLista() {
        // Arrange
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findAllByOrderByFechaProgramadaDesc())
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);

        // Act
        List<Recordatorio> resultado =
                recordatorioDataGateway.listarRecordatorios();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository).findAllByOrderByFechaProgramadaDesc();
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPorUsuario_debeConsultarYMapearLosRecordatorios() {
        // Arrange
        String idUsuario = "usuario-1";
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository
                .findByIdUsuarioOrderByFechaProgramadaDesc(idUsuario))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);

        // Act
        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByIdUsuarioOrderByFechaProgramadaDesc(idUsuario);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPendientes_debeBuscarPorEstadoPendienteYMapearResultados() {
        // Arrange
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findByEstadoOrderByFechaProgramadaAsc(
                EstadoRecordatorio.PENDIENTE.name()))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);

        // Act
        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPendientes();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByEstadoOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name());
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPendientesHasta_debeBuscarPendientesHastaLaFechaIndicada() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 10, 30);
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository
                .findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name(), fecha))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);

        // Act
        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPendientesHasta(fecha);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name(), fecha);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void existePendiente_cuandoExiste_debeRetornarTrue() {
        // Arrange
        String idUsuario = "usuario-1";
        String idPrestamo = "prestamo-1";
        String tipo = "RECORDATORIO_DEVOLUCION";

        when(recordatorioDataJpaRepository
                .existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
                        idUsuario,
                        idPrestamo,
                        tipo,
                        EstadoRecordatorio.PENDIENTE.name()))
                .thenReturn(true);

        // Act
        boolean resultado =
                recordatorioDataGateway.existePendiente(idUsuario, idPrestamo, tipo);

        // Assert
        assertTrue(resultado);
        verify(recordatorioDataJpaRepository)
                .existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
                        idUsuario,
                        idPrestamo,
                        tipo,
                        EstadoRecordatorio.PENDIENTE.name());
        verifyNoInteractions(recordatorioMapper);
    }

    @Test
    void existePendiente_cuandoNoExiste_debeRetornarFalse() {
        // Arrange
        String idUsuario = "usuario-1";
        String idPrestamo = "prestamo-1";
        String tipo = "RECORDATORIO_DEVOLUCION";

        when(recordatorioDataJpaRepository
                .existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
                        idUsuario,
                        idPrestamo,
                        tipo,
                        EstadoRecordatorio.PENDIENTE.name()))
                .thenReturn(false);

        // Act
        boolean resultado =
                recordatorioDataGateway.existePendiente(idUsuario, idPrestamo, tipo);

        // Assert
        assertFalse(resultado);
        verify(recordatorioDataJpaRepository)
                .existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
                        idUsuario,
                        idPrestamo,
                        tipo,
                        EstadoRecordatorio.PENDIENTE.name());
        verifyNoInteractions(recordatorioMapper);
    }

    @Test
    void eliminarRecordatorio_debeSolicitarEliminacionPorId() {
        // Arrange
        String id = "recordatorio-1";

        // Act
        recordatorioDataGateway.eliminarRecordatorio(id);

        // Assert
        verify(recordatorioDataJpaRepository).deleteById(id);
        verifyNoInteractions(recordatorioMapper);
    }
}