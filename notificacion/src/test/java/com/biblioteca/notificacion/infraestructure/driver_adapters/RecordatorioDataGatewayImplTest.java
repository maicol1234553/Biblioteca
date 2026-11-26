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


        Recordatorio resultado =
                recordatorioDataGateway.guardarRecordatorio(recordatorio);


        assertSame(recordatorioConvertido, resultado);
        verify(recordatorioMapper).toRecordatorioData(recordatorio);
        verify(recordatorioDataJpaRepository).save(recordatorioData);
        verify(recordatorioMapper).toRecordatorio(recordatorioDataGuardado);
    }

    @Test
    void actualizarRecordatorio_debeGuardarYRetornarRecordatorioActualizado() {

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


        Recordatorio resultado =
                recordatorioDataGateway.actualizarRecordatorio(recordatorio);


        assertSame(recordatorioActualizado, resultado);
        verify(recordatorioMapper).toRecordatorioData(recordatorio);
        verify(recordatorioDataJpaRepository).save(recordatorioData);
        verify(recordatorioMapper).toRecordatorio(recordatorioDataActualizado);
    }

    @Test
    void buscarRecordatorioPorId_cuandoExiste_debeRetornarRecordatorioMapeado() {

        String id = "recordatorio-1";
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findById(id))
                .thenReturn(Optional.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);


        Recordatorio resultado =
                recordatorioDataGateway.buscarRecordatorioPorId(id);


        assertSame(recordatorio, resultado);
        verify(recordatorioDataJpaRepository).findById(id);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void buscarRecordatorioPorId_cuandoNoExiste_debeRetornarNull() {

        String id = "recordatorio-inexistente";

        when(recordatorioDataJpaRepository.findById(id))
                .thenReturn(Optional.empty());


        Recordatorio resultado =
                recordatorioDataGateway.buscarRecordatorioPorId(id);


        assertEquals(null, resultado);
        verify(recordatorioDataJpaRepository).findById(id);
        verifyNoInteractions(recordatorioMapper);
    }

    @Test
    void listarRecordatorios_debeMapearYRetornarLaLista() {

        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findAllByOrderByFechaProgramadaDesc())
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);


        List<Recordatorio> resultado =
                recordatorioDataGateway.listarRecordatorios();


        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository).findAllByOrderByFechaProgramadaDesc();
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPorUsuario_debeConsultarYMapearLosRecordatorios() {

        String idUsuario = "usuario-1";
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository
                .findByIdUsuarioOrderByFechaProgramadaDesc(idUsuario))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);


        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPorUsuario(idUsuario);


        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByIdUsuarioOrderByFechaProgramadaDesc(idUsuario);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPendientes_debeBuscarPorEstadoPendienteYMapearResultados() {

        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository.findByEstadoOrderByFechaProgramadaAsc(
                EstadoRecordatorio.PENDIENTE.name()))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);


        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPendientes();


        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByEstadoOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name());
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void listarPendientesHasta_debeBuscarPendientesHastaLaFechaIndicada() {

        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 10, 30);
        RecordatorioData recordatorioData = mock(RecordatorioData.class);
        Recordatorio recordatorio = mock(Recordatorio.class);

        when(recordatorioDataJpaRepository
                .findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name(), fecha))
                .thenReturn(List.of(recordatorioData));
        when(recordatorioMapper.toRecordatorio(recordatorioData))
                .thenReturn(recordatorio);


        List<Recordatorio> resultado =
                recordatorioDataGateway.listarPendientesHasta(fecha);


        assertEquals(1, resultado.size());
        assertSame(recordatorio, resultado.get(0));
        verify(recordatorioDataJpaRepository)
                .findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name(), fecha);
        verify(recordatorioMapper).toRecordatorio(recordatorioData);
    }

    @Test
    void existePendiente_cuandoExiste_debeRetornarTrue() {

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


        boolean resultado =
                recordatorioDataGateway.existePendiente(idUsuario, idPrestamo, tipo);


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


        boolean resultado =
                recordatorioDataGateway.existePendiente(idUsuario, idPrestamo, tipo);


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

        String id = "recordatorio-1";


        recordatorioDataGateway.eliminarRecordatorio(id);


        verify(recordatorioDataJpaRepository).deleteById(id);
        verifyNoInteractions(recordatorioMapper);
    }
}