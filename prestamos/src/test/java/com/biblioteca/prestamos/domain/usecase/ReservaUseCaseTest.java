package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.TestFixtures;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LibroNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.ReservaDuplicadaException;
import com.biblioteca.prestamos.domain.exception.ReservaNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoEncontradoException;
import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaUseCaseTest {

    @Mock
    private ReservaGateway reservaGateway;
    @Mock
    private AuthGateway authGateway;
    @Mock
    private CatalogoGateway catalogoGateway;
    @Mock
    private NotificacionGateway notificacionGateway;

    private ReservaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReservaUseCase(
                reservaGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                TestFixtures.POLITICA,
                TestFixtures.CLOCK
        );
    }

    @Test
    void reservarLibro_creaReservaPendiente() {
        UsuarioExterno usuario = TestFixtures.usuario("u1");
        when(catalogoGateway.consultarDisponibilidadLibro("l1"))
                .thenReturn(new DisponibilidadLibroExterna("l1", "Libro", 2, 1, true));
        when(reservaGateway.existeReservaPendiente("u1", "l1")).thenReturn(false);
        when(reservaGateway.guardarReserva(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = useCase.reservarLibro("l1", "u1", usuario);

        assertNotNull(reserva.getIdReserva());
        assertEquals("u1", reserva.getIdUsuario());
        assertEquals("l1", reserva.getIdLibro());
        assertEquals("PENDIENTE", reserva.getEstado());
        assertEquals(TestFixtures.AHORA, reserva.getFechaReserva());
        assertEquals(TestFixtures.AHORA.plusDays(3), reserva.getFechaExpiracion());
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.RESERVA_REGISTRADA));
    }

    @Test
    void reservarLibro_adminReservaParaOtroUsuario() {
        when(authGateway.buscarUsuario("u2")).thenReturn(TestFixtures.usuario("u2"));
        when(catalogoGateway.consultarDisponibilidadLibro("l1"))
                .thenReturn(new DisponibilidadLibroExterna("l1", "Libro", 2, 1, true));
        when(reservaGateway.existeReservaPendiente("u2", "l1")).thenReturn(false);
        when(reservaGateway.guardarReserva(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = useCase.reservarLibro(
                "l1", "u2", TestFixtures.admin("admin-1"));

        assertEquals("u2", reserva.getIdUsuario());
        verify(authGateway).buscarUsuario("u2");
    }

    @Test
    void reservarLibro_rechazaUsuarioNoEncontrado() {
        when(authGateway.buscarUsuario("u2")).thenReturn(null);

        assertThrows(UsuarioNoEncontradoException.class,
                () -> useCase.reservarLibro("l1", "u2", TestFixtures.admin("admin-1")));

        verifyNoInteractions(catalogoGateway);
    }

    @Test
    void reservarLibro_rechazaUsuarioNoAutorizado() {
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.reservarLibro("l1", "u2", TestFixtures.usuario("u1")));

        verifyNoInteractions(authGateway, catalogoGateway);
    }

    @Test
    void reservarLibro_rechazaRolNoValido() {
        when(authGateway.buscarUsuario("u2"))
                .thenReturn(new UsuarioExterno("u2", "Usuario", "SUPERADMIN"));

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.reservarLibro("l1", "u2", TestFixtures.admin("admin-1")));

        verifyNoInteractions(catalogoGateway);
    }

    @Test
    void reservarLibro_rechazaIdentificadoresVacios() {
        UsuarioExterno usuario = TestFixtures.usuario("u1");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.reservarLibro("", "u1", usuario));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.reservarLibro("l1", "", usuario));
    }

    @Test
    void reservarLibro_rechazaLibroNoEncontrado() {
        when(catalogoGateway.consultarDisponibilidadLibro("l1")).thenReturn(null);

        assertThrows(LibroNoEncontradoException.class,
                () -> useCase.reservarLibro("l1", "u1", TestFixtures.usuario("u1")));
    }

    @Test
    void reservarLibro_rechazaReservaDuplicada() {
        when(catalogoGateway.consultarDisponibilidadLibro("l1"))
                .thenReturn(new DisponibilidadLibroExterna("l1", "Libro", 2, 1, true));
        when(reservaGateway.existeReservaPendiente("u1", "l1")).thenReturn(true);

        assertThrows(ReservaDuplicadaException.class,
                () -> useCase.reservarLibro("l1", "u1", TestFixtures.usuario("u1")));
    }

    @Test
    void cancelarReserva_cambiaEstado() {
        Reserva reserva = TestFixtures.reservaPendiente("r1", "u1", "l1");
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(reserva);
        when(reservaGateway.actualizarReserva(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reserva cancelada = useCase.cancelarReserva("r1", TestFixtures.usuario("u1"));

        assertEquals("CANCELADA", cancelada.getEstado());
    }

    @Test
    void cancelarReserva_rechazaEstadosYUsuariosInvalidos() {
        Reserva cancelada = TestFixtures.reserva("r1", "u1", "l1", "CANCELADA");
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(cancelada);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.cancelarReserva("r1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.cancelarReserva("r1", TestFixtures.usuario("u2")));
    }

    @Test
    void cancelarReserva_rechazaReservaNoEncontrada() {
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(null);

        assertThrows(ReservaNoEncontradaException.class,
                () -> useCase.cancelarReserva("r1", TestFixtures.usuario("u1")));
    }

    @Test
    void consultarReservasPorUsuario_validaPropietario() {
        when(reservaGateway.listarReservasPorUsuario("u1"))
                .thenReturn(List.of(TestFixtures.reservaPendiente("r1", "u1", "l1")));

        assertEquals(1, useCase.consultarReservasPorUsuario(
                "u1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarReservasPorUsuario(
                        "u1", TestFixtures.usuario("u2")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarReservasPorUsuario(
                        "", TestFixtures.usuario("u1")));
    }

    @Test
    void consultarReservasPendientesPorLibro_soloAdministradores() {
        when(reservaGateway.listarReservasPendientesPorLibro("l1"))
                .thenReturn(List.of(TestFixtures.reservaPendiente("r1", "u1", "l1")));

        assertEquals(1, useCase.consultarReservasPendientesPorLibro(
                "l1", TestFixtures.admin("admin-1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarReservasPendientesPorLibro(
                        "l1", TestFixtures.usuario("u1")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarReservasPendientesPorLibro(
                        "", TestFixtures.admin("admin-1")));
    }

    @Test
    void listarReservas_soloAdministradores() {
        when(reservaGateway.listarReservas()).thenReturn(List.of());

        assertTrue(useCase.listarReservas(TestFixtures.admin("admin-1")).isEmpty());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarReservas(TestFixtures.usuario("u1")));
    }

    @Test
    void buscarReserva_validaPropietario() {
        Reserva reserva = TestFixtures.reservaPendiente("r1", "u1", "l1");
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(reserva);

        assertEquals(reserva, useCase.buscarReserva("r1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarReserva("r1", TestFixtures.usuario("u2")));

        when(reservaGateway.buscarReservaPorId("r2")).thenReturn(null);
        assertThrows(ReservaNoEncontradaException.class,
                () -> useCase.buscarReserva("r2", TestFixtures.usuario("u1")));
    }

    @Test
    void expirarReservas_marcaExpiradasYNotifica() {
        Reserva reserva = TestFixtures.reservaPendiente("r1", "u1", "l1");
        when(reservaGateway.listarReservasPendientesExpiradas(TestFixtures.AHORA))
                .thenReturn(List.of(reserva));
        when(reservaGateway.actualizarReserva(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Reserva> expiradas = useCase.expirarReservas(TestFixtures.admin("admin-1"));

        assertEquals(1, expiradas.size());
        assertEquals("EXPIRADA", reserva.getEstado());
        verify(notificacionGateway).enviarEvento(argThat(evento ->
                evento.tipo() == TipoEventoNotificacion.RESERVA_EXPIRADA));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.expirarReservas(TestFixtures.usuario("u1")));
    }

    @Test
    void procesarReservasExpiradas_sinReservas() {
        when(reservaGateway.listarReservasPendientesExpiradas(TestFixtures.AHORA))
                .thenReturn(List.of());

        assertTrue(useCase.procesarReservasExpiradas().isEmpty());
        verifyNoInteractions(notificacionGateway);
    }

    @Test
    void eliminarReserva_permiteNoPendientes() {
        Reserva cancelada = TestFixtures.reserva("r1", "u1", "l1", "CANCELADA");
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(cancelada);

        useCase.eliminarReserva("r1", TestFixtures.admin("admin-1"));

        verify(reservaGateway).eliminarReserva("r1");
    }

    @Test
    void eliminarReserva_rechazaPendientesYNoAdministradores() {
        Reserva pendiente = TestFixtures.reservaPendiente("r1", "u1", "l1");
        when(reservaGateway.buscarReservaPorId("r1")).thenReturn(pendiente);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.eliminarReserva("r1", TestFixtures.admin("admin-1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.eliminarReserva("r1", TestFixtures.usuario("u1")));

        when(reservaGateway.buscarReservaPorId("r2")).thenReturn(null);
        assertThrows(ReservaNoEncontradaException.class,
                () -> useCase.eliminarReserva("r2", TestFixtures.admin("admin-1")));
    }
}
