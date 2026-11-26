package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.TestFixtures;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioNoEncontradoException;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioUseCaseTest {

    @Mock
    private RecordatorioGateway recordatorioGateway;
    @Mock
    private NotificacionUseCase notificacionUseCase;

    private RecordatorioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RecordatorioUseCase(
                recordatorioGateway,
                notificacionUseCase,
                TestFixtures.POLITICA,
                TestFixtures.CLOCK
        );
    }

    @Test
    void programarRecordatorio_conFechaExplicita() {
        LocalDateTime fecha = TestFixtures.AHORA.plusDays(2);
        when(recordatorioGateway.existePendiente(
                "u1", "p1", "DEVOLUCION_PROXIMA")).thenReturn(false);
        when(recordatorioGateway.guardarRecordatorio(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Recordatorio recordatorio = useCase.programarRecordatorioDevolucion(
                "u1", "p1", fecha, "DEVOLUCION_PROXIMA");

        assertEquals("u1", recordatorio.getIdUsuario());
        assertEquals("p1", recordatorio.getIdPrestamo());
        assertEquals(fecha, recordatorio.getFechaProgramada());
        assertEquals("DEVOLUCION_PROXIMA", recordatorio.getTipo());
        assertEquals("PENDIENTE", recordatorio.getEstado());
    }

    @Test
    void programarRecordatorio_usaFechaPorDefecto() {
        when(recordatorioGateway.existePendiente(
                "u1", "p1", "DEVOLUCION_PROXIMA")).thenReturn(false);
        when(recordatorioGateway.guardarRecordatorio(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Recordatorio recordatorio = useCase.programarRecordatorioDevolucion(
                "u1", "p1", null, null);

        assertEquals(TestFixtures.AHORA.plusDays(1), recordatorio.getFechaProgramada());
        assertEquals("DEVOLUCION_PROXIMA", recordatorio.getTipo());
    }

    @Test
    void programarRecordatorio_rechazaFechasPasadasEIdsVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.programarRecordatorioDevolucion(
                        "u1", "p1", TestFixtures.AHORA.minusDays(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.programarRecordatorioDevolucion(
                        "", "p1", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.programarRecordatorioDevolucion(
                        "u1", "", null, null));
    }

    @Test
    void programarRecordatorio_rechazaDuplicados() {
        when(recordatorioGateway.existePendiente(
                "u1", "p1", "DEVOLUCION_PROXIMA")).thenReturn(true);

        assertThrows(RecordatorioDuplicadoException.class,
                () -> useCase.programarRecordatorioDevolucion(
                        "u1", "p1", TestFixtures.AHORA.plusDays(2), null));
    }

    @Test
    void programarRecordatorio_rechazaTipoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.programarRecordatorioDevolucion(
                        "u1", "p1", TestFixtures.AHORA.plusDays(2), "OTRO"));
    }

    @Test
    void cancelarRecordatorio_cambiaEstado() {
        Recordatorio recordatorio = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.plusDays(1),
                "DEVOLUCION_PROXIMA", "PENDIENTE");
        when(recordatorioGateway.buscarRecordatorioPorId("r1")).thenReturn(recordatorio);
        when(recordatorioGateway.actualizarRecordatorio(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Recordatorio cancelado = useCase.cancelarRecordatorio("r1");

        assertEquals("CANCELADO", cancelado.getEstado());
    }

    @Test
    void cancelarRecordatorio_rechazaEstadosYNoEncontrados() {
        Recordatorio cancelado = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.plusDays(1),
                "DEVOLUCION_PROXIMA", "CANCELADO");
        when(recordatorioGateway.buscarRecordatorioPorId("r1")).thenReturn(cancelado);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.cancelarRecordatorio("r1"));

        when(recordatorioGateway.buscarRecordatorioPorId("r2")).thenReturn(null);
        assertThrows(RecordatorioNoEncontradoException.class,
                () -> useCase.cancelarRecordatorio("r2"));
    }

    @Test
    void listarYBuscarRecordatorios() {
        Recordatorio recordatorio = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.plusDays(1),
                "DEVOLUCION_PROXIMA", "PENDIENTE");
        when(recordatorioGateway.listarPendientes()).thenReturn(List.of(recordatorio));
        when(recordatorioGateway.listarPorUsuario("u1")).thenReturn(List.of(recordatorio));
        when(recordatorioGateway.listarRecordatorios()).thenReturn(List.of(recordatorio));
        when(recordatorioGateway.buscarRecordatorioPorId("r1")).thenReturn(recordatorio);

        assertEquals(1, useCase.listarPendientes().size());
        assertEquals(1, useCase.listarPorUsuario("u1").size());
        assertEquals(1, useCase.listarRecordatorios().size());
        assertEquals(recordatorio, useCase.buscarRecordatorio("r1"));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.listarPorUsuario(""));
        assertThrows(RecordatorioNoEncontradoException.class,
                () -> useCase.buscarRecordatorio("r2"));
    }

    @Test
    void procesarRecordatoriosPendientes_enviaYMarcaIProcesados() {
        Recordatorio devolucion = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.minusMinutes(1),
                "DEVOLUCION_PROXIMA", "PENDIENTE");
        Recordatorio vencido = TestFixtures.recordatorio(
                "r2", "u2", "p2", TestFixtures.AHORA.minusMinutes(1),
                "PRESTAMO_VENCIDO", "PENDIENTE");
        when(recordatorioGateway.listarPendientesHasta(TestFixtures.AHORA))
                .thenReturn(List.of(devolucion, vencido));
        when(recordatorioGateway.actualizarRecordatorio(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Recordatorio> procesados = useCase.procesarRecordatoriosPendientes();

        assertEquals(2, procesados.size());
        assertEquals("PROCESADO", devolucion.getEstado());
        assertEquals("PROCESADO", vencido.getEstado());
        verify(notificacionUseCase).enviar(
                eq("u1"), eq(null), eq(null), contains("proximo a vencer"),
                eq("DEVOLUCION_PROXIMA"));
        verify(notificacionUseCase).enviar(
                eq("u2"), eq(null), eq(null), contains("vencido"),
                eq("PRESTAMO_VENCIDO"));
    }

    @Test
    void procesarRecordatoriosPendientes_continuaSiFallaElEnvio() {
        Recordatorio recordatorio = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.minusMinutes(1),
                "DEVOLUCION_PROXIMA", "PENDIENTE");
        when(recordatorioGateway.listarPendientesHasta(TestFixtures.AHORA))
                .thenReturn(List.of(recordatorio));
        doThrow(new RuntimeException("smtp"))
                .when(notificacionUseCase).enviar(
                        anyString(), any(), any(), anyString(), anyString());
        when(recordatorioGateway.actualizarRecordatorio(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Recordatorio> procesados = useCase.procesarRecordatoriosPendientes();

        assertEquals(1, procesados.size());
        assertEquals("PROCESADO", recordatorio.getEstado());
        verify(recordatorioGateway).actualizarRecordatorio(recordatorio);
    }

    @Test
    void procesarRecordatoriosPendientes_sinPendientes() {
        when(recordatorioGateway.listarPendientesHasta(TestFixtures.AHORA))
                .thenReturn(List.of());

        assertTrue(useCase.procesarRecordatoriosPendientes().isEmpty());
    }

    @Test
    void eliminarRecordatorio_permiteNoPendientes() {
        Recordatorio recordatorio = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.plusDays(1),
                "DEVOLUCION_PROXIMA", "PROCESADO");
        when(recordatorioGateway.buscarRecordatorioPorId("r1")).thenReturn(recordatorio);

        useCase.eliminarRecordatorio("r1");

        verify(recordatorioGateway).eliminarRecordatorio("r1");
    }

    @Test
    void eliminarRecordatorio_rechazaPendientesYNoEncontrados() {
        Recordatorio pendiente = TestFixtures.recordatorio(
                "r1", "u1", "p1", TestFixtures.AHORA.plusDays(1),
                "DEVOLUCION_PROXIMA", "PENDIENTE");
        when(recordatorioGateway.buscarRecordatorioPorId("r1")).thenReturn(pendiente);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.eliminarRecordatorio("r1"));

        when(recordatorioGateway.buscarRecordatorioPorId("r2")).thenReturn(null);
        assertThrows(RecordatorioNoEncontradoException.class,
                () -> useCase.eliminarRecordatorio("r2"));
    }
}
