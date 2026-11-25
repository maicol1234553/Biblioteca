package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.TestFixtures;
import com.biblioteca.prestamos.domain.exception.EjemplarNoDisponibleException;
import com.biblioteca.prestamos.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LimitePrestamosExcedidoException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoEncontradoException;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoUseCaseTest {

    @Mock
    private PrestamoGateway prestamoGateway;
    @Mock
    private ReservaGateway reservaGateway;
    @Mock
    private MultaGateway multaGateway;
    @Mock
    private DevolucionGateway devolucionGateway;
    @Mock
    private AuthGateway authGateway;
    @Mock
    private CatalogoGateway catalogoGateway;
    @Mock
    private NotificacionGateway notificacionGateway;

    private PrestamoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PrestamoUseCase(
                prestamoGateway,
                reservaGateway,
                multaGateway,
                devolucionGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                TestFixtures.POLITICA,
                TestFixtures.CLOCK
        );
    }

    @Test
    void solicitarPrestamo_creaPrestamoYConvierteReserva() {
        UsuarioExterno usuario = TestFixtures.usuario("u1");

        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(false);
        when(prestamoGateway.contarPrestamosActivosPorUsuario("u1")).thenReturn(0L);
        when(prestamoGateway.guardarPrestamo(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var reserva = TestFixtures.reservaPendiente("r1", "u1", "l1");
        when(reservaGateway.buscarReservaPendiente("u1", "l1")).thenReturn(reserva);
        when(reservaGateway.actualizarReserva(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo prestamo = useCase.solicitarPrestamo("u1", "e1", usuario);

        assertNotNull(prestamo.getIdPrestamo());
        assertEquals("u1", prestamo.getIdUsuario());
        assertEquals("e1", prestamo.getIdEjemplar());
        assertEquals("ACTIVO", prestamo.getEstado());
        assertEquals(TestFixtures.AHORA, prestamo.getFechaPrestamo());
        assertEquals(TestFixtures.AHORA.plusDays(15), prestamo.getFechaVencimiento());
        assertEquals("CONVERTIDA", reserva.getEstado());

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "PRESTADO");
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.PRESTAMO_REGISTRADO
                        && evento.referenciaId().equals(prestamo.getIdPrestamo())));
    }

    @Test
    void solicitarPrestamo_adminSolicitaParaOtroUsuario() {
        UsuarioExterno admin = TestFixtures.admin("admin-1");

        when(authGateway.buscarUsuario("u2")).thenReturn(TestFixtures.usuario("u2"));
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(false);
        when(prestamoGateway.contarPrestamosActivosPorUsuario("u2")).thenReturn(1L);
        when(prestamoGateway.guardarPrestamo(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo prestamo = useCase.solicitarPrestamo("u2", "e1", admin);

        assertEquals("u2", prestamo.getIdUsuario());
        verify(authGateway).buscarUsuario("u2");
    }

    @Test
    void solicitarPrestamo_rechazaUsuarioNoEncontrado() {
        when(authGateway.buscarUsuario("u2")).thenReturn(null);

        assertThrows(UsuarioNoEncontradoException.class,
                () -> useCase.solicitarPrestamo("u2", "e1", TestFixtures.admin("admin-1")));

        verifyNoInteractions(catalogoGateway);
    }

    @Test
    void solicitarPrestamo_rechazaUsuarioNoAutorizado() {
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.solicitarPrestamo("u2", "e1", TestFixtures.usuario("u1")));

        verifyNoInteractions(authGateway, catalogoGateway);
    }

    @Test
    void solicitarPrestamo_rechazaRolNoValido() {
        when(authGateway.buscarUsuario("u2"))
                .thenReturn(new UsuarioExterno("u2", "Usuario", "SUPERADMIN"));

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.solicitarPrestamo("u2", "e1", TestFixtures.admin("admin-1")));

        verifyNoInteractions(catalogoGateway);
    }

    @Test
    void solicitarPrestamo_rechazaIdentificadoresVacios() {
        UsuarioExterno usuario = TestFixtures.usuario("u1");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.solicitarPrestamo("", "e1", usuario));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.solicitarPrestamo("u1", "", usuario));
    }

    @Test
    void solicitarPrestamo_rechazaEjemplarNoEncontrado() {
        when(catalogoGateway.buscarEjemplar("e1")).thenReturn(null);

        assertThrows(EjemplarNoEncontradoException.class,
                () -> useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1")));

        verifyNoInteractions(prestamoGateway);
    }

    @Test
    void solicitarPrestamo_rechazaEjemplarNoDisponible() {
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "PRESTADO"));

        assertThrows(EjemplarNoDisponibleException.class,
                () -> useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1")));
    }

    @Test
    void solicitarPrestamo_rechazaPrestamoActivoDelEjemplar() {
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(true);

        assertThrows(EjemplarNoDisponibleException.class,
                () -> useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1")));
    }

    @Test
    void solicitarPrestamo_rechazaLimiteDePrestamos() {
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(false);
        when(prestamoGateway.contarPrestamosActivosPorUsuario("u1")).thenReturn(3L);

        assertThrows(LimitePrestamosExcedidoException.class,
                () -> useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1")));
    }

    @Test
    void solicitarPrestamo_revierteCatalogoSiFallaElGuardado() {
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(false);
        when(prestamoGateway.contarPrestamosActivosPorUsuario("u1")).thenReturn(0L);
        when(prestamoGateway.guardarPrestamo(any())).thenThrow(new RuntimeException("db"));

        assertThrows(RuntimeException.class,
                () -> useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1")));

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "PRESTADO");
        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "DISPONIBLE");
    }

    @Test
    void solicitarPrestamo_noFallaSiNoPuedeConvertirLaReserva() {
        when(catalogoGateway.buscarEjemplar("e1"))
                .thenReturn(TestFixtures.ejemplar("e1", "l1", "DISPONIBLE"));
        when(prestamoGateway.existePrestamoActivoPorEjemplar("e1")).thenReturn(false);
        when(prestamoGateway.contarPrestamosActivosPorUsuario("u1")).thenReturn(0L);
        when(prestamoGateway.guardarPrestamo(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservaGateway.buscarReservaPendiente("u1", "l1"))
                .thenThrow(new RuntimeException("reserva"));

        Prestamo prestamo = useCase.solicitarPrestamo("u1", "e1", TestFixtures.usuario("u1"));

        assertNotNull(prestamo);
        verify(notificacionGateway).enviarEvento(any());
    }

    @Test
    void devolverPrestamo_registraDevolucionSinMulta() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(5), "ACTIVO");
        prepararDevolucion(prestamo);

        Devolucion devolucion = useCase.devolverPrestamo(
                "p1", "BUENO", "Sin novedad", TestFixtures.usuario("u1"));

        assertEquals("DEVUELTO", prestamo.getEstado());
        assertEquals(TestFixtures.AHORA, prestamo.getFechaDevolucion());
        assertEquals("BUENO", devolucion.getEstadoEjemplar());
        assertEquals("Sin novedad", devolucion.getObservaciones());

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "DISPONIBLE");
        verify(multaGateway, never()).buscarMultaPendientePorPrestamo(any());
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.DEVOLUCION_REGISTRADA));
    }

    @Test
    void devolverPrestamo_generaMultaPorRetraso() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(5), "ACTIVO");
        prepararDevolucion(prestamo);
        when(multaGateway.buscarMultaPendientePorPrestamo("p1")).thenReturn(null);

        useCase.devolverPrestamo("p1", "DANADO", null, TestFixtures.usuario("u1"));

        ArgumentCaptor<Multa> captor = ArgumentCaptor.forClass(Multa.class);
        verify(multaGateway).guardarMulta(captor.capture());
        assertEquals(0, new BigDecimal("10000.00").compareTo(captor.getValue().getValor()));
        assertEquals("PENDIENTE", captor.getValue().getEstado());
        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "MANTENIMIENTO");
    }

    @Test
    void devolverPrestamo_actualizaMultaPendienteExistente() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(2), "VENCIDO");
        prepararDevolucion(prestamo);
        Multa multa = TestFixtures.multaPendiente("m1", "p1");
        when(multaGateway.buscarMultaPendientePorPrestamo("p1")).thenReturn(multa);

        useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1"));

        verify(multaGateway).actualizarMulta(multa);
        verify(multaGateway, never()).guardarMulta(any());
        assertEquals(0, new BigDecimal("4000.00").compareTo(multa.getValor()));
    }

    @Test
    void devolverPrestamo_rechazaPrestamoYaDevuelto() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(2), "DEVUELTO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertThrows(PrestamoYaDevueltoException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));

        verifyNoInteractions(devolucionGateway, catalogoGateway);
    }

    @Test
    void devolverPrestamo_rechazaDevolucionExistente() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo("p1"))
                .thenReturn(new Devolucion("d1", "p1", TestFixtures.AHORA, null, "BUENO"));

        assertThrows(PrestamoYaDevueltoException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));
    }

    @Test
    void devolverPrestamo_rechazaPrestamoCancelado() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "CANCELADO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo("p1")).thenReturn(null);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));
    }

    @Test
    void devolverPrestamo_rechazaPrestamoNoEncontrado() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));
    }

    @Test
    void devolverPrestamo_rechazaUsuarioNoPropietario() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u2")));
    }

    @Test
    void devolverPrestamo_revierteCatalogoSiFallaElGuardado() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo("p1")).thenReturn(null);
        when(prestamoGateway.actualizarPrestamo(any())).thenThrow(new RuntimeException("db"));

        assertThrows(RuntimeException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "DISPONIBLE");
        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "PRESTADO");
    }

    @Test
    void devolverPrestamo_revierteCatalogoSiFallaLaMulta() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(3), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo("p1")).thenReturn(null);
        when(multaGateway.buscarMultaPendientePorPrestamo("p1")).thenReturn(null);
        when(multaGateway.guardarMulta(any())).thenThrow(new RuntimeException("db"));

        assertThrows(RuntimeException.class,
                () -> useCase.devolverPrestamo("p1", "BUENO", null, TestFixtures.usuario("u1")));

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "PRESTADO");
    }

    @Test
    void consultarMisPrestamos_validaPropietario() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.listarPrestamosActivosPorUsuario("u1")).thenReturn(List.of(prestamo));

        List<Prestamo> prestamos = useCase.consultarMisPrestamos(
                "u1", TestFixtures.usuario("u1"));

        assertEquals(1, prestamos.size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarMisPrestamos("u1", TestFixtures.usuario("u2")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarMisPrestamos("", TestFixtures.usuario("u1")));
    }

    @Test
    void consultarHistorial_validaPropietario() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
        when(prestamoGateway.listarHistorialPorUsuario("u1")).thenReturn(List.of(prestamo));

        List<Prestamo> prestamos = useCase.consultarHistorial(
                "u1", TestFixtures.usuario("u1"));

        assertEquals(1, prestamos.size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarHistorial("u1", TestFixtures.usuario("u2")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarHistorial("", TestFixtures.usuario("u1")));
    }

    @Test
    void consultarPrestamosVencidos_marcaComoVencidosYGeneraMulta() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.minusDays(2), "ACTIVO");
        when(prestamoGateway.listarPrestamosVencidos(TestFixtures.AHORA))
                .thenReturn(List.of(prestamo));
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(multaGateway.buscarMultaPendientePorPrestamo("p1"))
                .thenReturn(TestFixtures.multaPendiente("m1", "p1"));

        List<Prestamo> vencidos = useCase.consultarPrestamosVencidos(
                TestFixtures.admin("admin-1"));

        assertEquals(1, vencidos.size());
        assertEquals("VENCIDO", prestamo.getEstado());
        verify(multaGateway).actualizarMulta(any());
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.PRESTAMO_VENCIDO));
    }

    @Test
    void consultarPrestamosVencidos_rechazaUsuarioNoAdministrador() {
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarPrestamosVencidos(TestFixtures.usuario("u1")));
    }

    @Test
    void procesarPrestamosVencidos_sinPrestamos() {
        when(prestamoGateway.listarPrestamosVencidos(TestFixtures.AHORA)).thenReturn(List.of());

        assertTrue(useCase.procesarPrestamosVencidos().isEmpty());
        verifyNoInteractions(notificacionGateway);
    }

    @Test
    void buscarPrestamoPorId_validaExistenciaYPropietario() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertEquals(prestamo, useCase.buscarPrestamoPorId("p1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarPrestamoPorId("p1", TestFixtures.usuario("u2")));

        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(null);
        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.buscarPrestamoPorId("p2", TestFixtures.usuario("u1")));
    }

    @Test
    void listarPrestamos_soloAdministradores() {
        when(prestamoGateway.listarPrestamos()).thenReturn(List.of());

        assertTrue(useCase.listarPrestamos(TestFixtures.admin("admin-1")).isEmpty());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarPrestamos(TestFixtures.usuario("u1")));
    }

    @Test
    void actualizarVencimiento_actualizaFechaYEstado() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime nuevaFecha = TestFixtures.AHORA.plusDays(10);
        Prestamo actualizado = useCase.actualizarVencimiento(
                "p1", nuevaFecha, TestFixtures.admin("admin-1"));

        assertEquals(nuevaFecha, actualizado.getFechaVencimiento());
        assertEquals("ACTIVO", actualizado.getEstado());
    }

    @Test
    void actualizarVencimiento_marcaVencidoSiLaFechaEsPasada() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo actualizado = useCase.actualizarVencimiento(
                "p1", TestFixtures.AHORA.minusDays(1), TestFixtures.admin("admin-1"));

        assertEquals("VENCIDO", actualizado.getEstado());
    }

    @Test
    void actualizarVencimiento_rechazaEstadosYFechasInvalidas() {
        Prestamo devuelto = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(devuelto);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.actualizarVencimiento(
                        "p1", TestFixtures.AHORA.plusDays(5), TestFixtures.admin("admin-1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.actualizarVencimiento(
                        "p1", TestFixtures.AHORA.plusDays(5), TestFixtures.usuario("u1")));

        Prestamo activo = TestFixtures.prestamo(
                "p2", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(activo);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.actualizarVencimiento(
                        "p2", null, TestFixtures.admin("admin-1")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.actualizarVencimiento(
                        "p2", TestFixtures.AHORA.minusDays(20), TestFixtures.admin("admin-1")));
    }

    @Test
    void cancelarPrestamo_liberaElEjemplar() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo cancelado = useCase.cancelarPrestamo("p1", TestFixtures.admin("admin-1"));

        assertEquals("CANCELADO", cancelado.getEstado());
        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "DISPONIBLE");
    }

    @Test
    void cancelarPrestamo_revierteCatalogoSiFallaElGuardado() {
        Prestamo prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(prestamoGateway.actualizarPrestamo(any())).thenThrow(new RuntimeException("db"));

        assertThrows(RuntimeException.class,
                () -> useCase.cancelarPrestamo("p1", TestFixtures.admin("admin-1")));

        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "DISPONIBLE");
        verify(catalogoGateway).actualizarEstadoEjemplar("e1", "PRESTADO");
    }

    @Test
    void cancelarPrestamo_rechazaEstadosInvalidos() {        Prestamo devuelto = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(devuelto);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.cancelarPrestamo("p1", TestFixtures.admin("admin-1")));

        Prestamo cancelado = TestFixtures.prestamo(
                "p2", "u1", "e1", TestFixtures.AHORA.plusDays(2), "CANCELADO");
        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(cancelado);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.cancelarPrestamo("p2", TestFixtures.admin("admin-1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.cancelarPrestamo("p2", TestFixtures.usuario("u1")));
    }

    @Test
    void eliminarPrestamo_permiteDevueltosYCancelados() {
        Prestamo devuelto = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(devuelto);

        useCase.eliminarPrestamo("p1", TestFixtures.admin("admin-1"));

        verify(prestamoGateway).eliminarPrestamo("p1");
    }

    @Test
    void eliminarPrestamo_rechazaActivosYNoAdministradores() {
        Prestamo activo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(activo);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.eliminarPrestamo("p1", TestFixtures.admin("admin-1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.eliminarPrestamo("p1", TestFixtures.usuario("u1")));

        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(null);
        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.eliminarPrestamo("p2", TestFixtures.admin("admin-1")));
    }

    private void prepararDevolucion(Prestamo prestamo) {
        when(prestamoGateway.buscarPrestamoPorId(prestamo.getIdPrestamo())).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo(prestamo.getIdPrestamo()))
                .thenReturn(null);
        when(prestamoGateway.actualizarPrestamo(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(devolucionGateway.guardarDevolucion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
