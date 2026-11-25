package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.TestFixtures;
import com.biblioteca.prestamos.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LimiteRenovacionesExcedidoException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.biblioteca.prestamos.domain.exception.RenovacionNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.RenovacionNoPermitidaException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.RenovacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenovacionUseCaseTest {

    @Mock
    private PrestamoGateway prestamoGateway;
    @Mock
    private ReservaGateway reservaGateway;
    @Mock
    private RenovacionGateway renovacionGateway;
    @Mock
    private CatalogoGateway catalogoGateway;
    @Mock
    private NotificacionGateway notificacionGateway;

    private RenovacionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RenovacionUseCase(
                prestamoGateway,
                reservaGateway,
                renovacionGateway,
                catalogoGateway,
                notificacionGateway,
                TestFixtures.POLITICA,
                TestFixtures.CLOCK
        );
    }

    @Test
    void renovarPrestamo_apruebaRenovacion() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.contarRenovacionesPorPrestamo("p1")).thenReturn(0L);
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "PRESTADO"));
        when(reservaGateway.listarReservasPendientesPorLibro("l1")).thenReturn(List.of());
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(renovacionGateway.guardarRenovacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime fechaAnterior = prestamo.getFechaVencimiento();
        Prestamo renovado = useCase.renovarPrestamo("p1", TestFixtures.usuario("u1"));

        assertEquals(fechaAnterior.plusDays(15), renovado.getFechaVencimiento());
        assertEquals("ACTIVO", renovado.getEstado());

        ArgumentCaptor<Renovacion> captor = ArgumentCaptor.forClass(Renovacion.class);
        verify(renovacionGateway).guardarRenovacion(captor.capture());
        assertEquals(fechaAnterior, captor.getValue().getFechaAnterior());
        assertEquals(fechaAnterior.plusDays(15), captor.getValue().getFechaNueva());
        assertEquals("APROBADA", captor.getValue().getEstado());
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.RENOVACION_REGISTRADA));
    }

    @Test
    void renovarPrestamo_rechazaPrestamoNoEncontrado() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaUsuarioNoPropietario() {
        when(prestamoGateway.buscarPrestamoPorId("p1"))
                .thenReturn(prestamoActivo(TestFixtures.AHORA.plusDays(2)));

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u2")));

        verifyNoInteractions(renovacionGateway, catalogoGateway);
    }

    @Test
    void renovarPrestamo_rechazaPrestamoDevuelto() {
        Prestamo devuelto = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(devuelto);

        assertThrows(PrestamoYaDevueltoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaPrestamoCancelado() {
        Prestamo cancelado = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "CANCELADO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(cancelado);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaPrestamoVencido() {
        Prestamo vencido = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(1), "VENCIDO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(vencido);

        assertThrows(RenovacionNoPermitidaException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaFechaVencidaAunqueElEstadoSeaActivo() {
        Prestamo activo = prestamoActivo(TestFixtures.AHORA.minusDays(1));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(activo);

        assertThrows(RenovacionNoPermitidaException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaLimiteDeRenovaciones() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.contarRenovacionesPorPrestamo("p1")).thenReturn(2L);

        assertThrows(LimiteRenovacionesExcedidoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaEjemplarNoEncontrado() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.contarRenovacionesPorPrestamo("p1")).thenReturn(0L);
        when(catalogoGateway.buscarEjemplar("e1")).thenReturn(null);

        assertThrows(EjemplarNoEncontradoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaEjemplarSinLibro() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.contarRenovacionesPorPrestamo("p1")).thenReturn(0L);
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", null, "PRESTADO"));

        assertThrows(EjemplarNoEncontradoException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void renovarPrestamo_rechazaReservasPendientes() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.contarRenovacionesPorPrestamo("p1")).thenReturn(0L);
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "PRESTADO"));
        when(reservaGateway.listarReservasPendientesPorLibro("l1"))
                .thenReturn(List.of(TestFixtures.reservaPendiente("r1", "u2", "l1")));

        assertThrows(RenovacionNoPermitidaException.class,
                () -> useCase.renovarPrestamo("p1", TestFixtures.usuario("u1")));
    }

    @Test
    void listarRenovacionesPorPrestamo_validaPropietario() {
        Prestamo prestamo = prestamoActivo(TestFixtures.AHORA.plusDays(2));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(renovacionGateway.listarRenovacionesPorPrestamo("p1"))
                .thenReturn(List.of(renovacion("rn1", "p1")));

        assertEquals(1, useCase.listarRenovacionesPorPrestamo(
                "p1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarRenovacionesPorPrestamo(
                        "p1", TestFixtures.usuario("u2")));
    }

    @Test
    void listarRenovacionesPorPrestamo_rechazaPrestamoNoEncontrado() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.listarRenovacionesPorPrestamo(
                        "p1", TestFixtures.usuario("u1")));
    }

    @Test
    void listarRenovaciones_soloAdministradores() {
        when(renovacionGateway.listarRenovaciones()).thenReturn(List.of());

        assertTrue(useCase.listarRenovaciones(TestFixtures.admin("admin-1")).isEmpty());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarRenovaciones(TestFixtures.usuario("u1")));
    }

    @Test
    void buscarRenovacion_validaExistenciaYPropietario() {
        Renovacion renovacion = renovacion("rn1", "p1");
        when(renovacionGateway.buscarRenovacionPorId("rn1")).thenReturn(renovacion);
        when(prestamoGateway.buscarPrestamoPorId("p1"))
                .thenReturn(prestamoActivo(TestFixtures.AHORA.plusDays(2)));

        assertEquals(renovacion, useCase.buscarRenovacion(
                "rn1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarRenovacion("rn1", TestFixtures.usuario("u2")));

        when(renovacionGateway.buscarRenovacionPorId("rn2")).thenReturn(null);
        assertThrows(RenovacionNoEncontradaException.class,
                () -> useCase.buscarRenovacion("rn2", TestFixtures.usuario("u1")));
    }

    @Test
    void buscarRenovacion_rechazaPrestamoNoEncontrado() {
        when(renovacionGateway.buscarRenovacionPorId("rn1"))
                .thenReturn(renovacion("rn1", "p1"));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.buscarRenovacion("rn1", TestFixtures.usuario("u1")));
    }

    @Test
    void eliminarRenovacion_permiteAdministradores() {
        when(renovacionGateway.buscarRenovacionPorId("rn1"))
                .thenReturn(renovacion("rn1", "p1"));

        useCase.eliminarRenovacion("rn1", TestFixtures.admin("admin-1"));

        verify(renovacionGateway).eliminarRenovacion("rn1");
    }

    @Test
    void eliminarRenovacion_rechazaNoEncontradasYNoAdministradores() {
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.eliminarRenovacion("rn1", TestFixtures.usuario("u1")));

        when(renovacionGateway.buscarRenovacionPorId("rn1")).thenReturn(null);
        assertThrows(RenovacionNoEncontradaException.class,
                () -> useCase.eliminarRenovacion("rn1", TestFixtures.admin("admin-1")));
    }

    private Prestamo prestamoActivo(LocalDateTime fechaVencimiento) {
        return TestFixtures.prestamo("p1", "u1", "e1", fechaVencimiento, "ACTIVO");
    }

    private Renovacion renovacion(String id, String idPrestamo) {
        return new Renovacion(
                id,
                idPrestamo,
                TestFixtures.AHORA,
                TestFixtures.AHORA.plusDays(2),
                TestFixtures.AHORA.plusDays(17),
                "APROBADA"
        );
    }
}
