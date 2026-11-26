package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LibroNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.ReservaDuplicadaException;
import com.biblioteca.prestamos.domain.exception.ReservaNoEncontradaException;
import com.biblioteca.prestamos.domain.model.Autorizacion;
import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.domain.model.EstadoReserva;
import com.biblioteca.prestamos.domain.model.PoliticaPrestamos;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ReservaUseCase {

    private final ReservaGateway reservaGateway;
    private final AuthGateway authGateway;
    private final CatalogoGateway catalogoGateway;
    private final NotificacionGateway notificacionGateway;
    private final PoliticaPrestamos politica;
    private final Clock clock;

    public Reserva reservarLibro(String idLibro,
                                 String idUsuario,
                                 UsuarioExterno solicitante) {
        if (esBlanco(idLibro)) {
            throw new IllegalArgumentException("El libro es obligatorio");
        }

        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);
        UsuarioExterno usuario = ValidadorUsuario.validar(authGateway, solicitante, idUsuario);

        DisponibilidadLibroExterna libro = catalogoGateway.consultarDisponibilidadLibro(idLibro);

        if (libro == null) {
            throw new LibroNoEncontradoException("Libro no encontrado con id: " + idLibro);
        }

        if (reservaGateway.existeReservaPendiente(idUsuario, idLibro)) {
            throw new ReservaDuplicadaException(
                    "El usuario ya tiene una reserva pendiente para este libro");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        Reserva reserva = new Reserva();
        reserva.setIdReserva(UUID.randomUUID().toString());
        reserva.setIdUsuario(usuario.idUsuario());
        reserva.setIdLibro(idLibro);
        reserva.setFechaReserva(ahora);
        reserva.setFechaExpiracion(ahora.plusDays(politica.diasReserva()));
        reserva.setEstado(EstadoReserva.PENDIENTE.name());

        Reserva guardada = reservaGateway.guardarReserva(reserva);

        notificacionGateway.enviarEvento(new EventoNotificacion(
                TipoEventoNotificacion.RESERVA_REGISTRADA,
                usuario.idUsuario(),
                guardada.getIdReserva(),
                "Reserva registrada para el libro " + idLibro
                        + ". Expira el " + guardada.getFechaExpiracion(),
                ahora
        ));

        return guardada;
    }

    public Reserva cancelarReserva(String idReserva, UsuarioExterno solicitante) {
        Reserva reserva = buscarReservaInterna(idReserva);
        Autorizacion.exigirAcceso(solicitante, reserva.getIdUsuario());

        if (!EstadoReserva.PENDIENTE.name().equals(reserva.getEstado())) {
            throw new EstadoInvalidoException(
                    "Solo se pueden cancelar reservas pendientes");
        }

        reserva.setEstado(EstadoReserva.CANCELADA.name());

        return reservaGateway.actualizarReserva(reserva);
    }

    public List<Reserva> consultarReservasPorUsuario(String idUsuario,
                                                     UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);

        return reservaGateway.listarReservasPorUsuario(idUsuario);
    }

    public List<Reserva> consultarReservasPendientesPorLibro(String idLibro,
                                                             UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        if (esBlanco(idLibro)) {
            throw new IllegalArgumentException("El libro es obligatorio");
        }

        return reservaGateway.listarReservasPendientesPorLibro(idLibro);
    }

    public List<Reserva> listarReservas(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return reservaGateway.listarReservas();
    }

    public Reserva buscarReserva(String idReserva, UsuarioExterno solicitante) {
        Reserva reserva = buscarReservaInterna(idReserva);
        Autorizacion.exigirAcceso(solicitante, reserva.getIdUsuario());

        return reserva;
    }

    public List<Reserva> expirarReservas(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return procesarReservasExpiradas();
    }




    public List<Reserva> procesarReservasExpiradas() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Reserva> expiradas = reservaGateway.listarReservasPendientesExpiradas(ahora);

        for (Reserva reserva : expiradas) {
            reserva.setEstado(EstadoReserva.EXPIRADA.name());
            reservaGateway.actualizarReserva(reserva);

            notificacionGateway.enviarEvento(new EventoNotificacion(
                    TipoEventoNotificacion.RESERVA_EXPIRADA,
                    reserva.getIdUsuario(),
                    reserva.getIdReserva(),
                    "La reserva del libro " + reserva.getIdLibro() + " expiro",
                    ahora
            ));
        }

        return expiradas;
    }

    public void eliminarReserva(String idReserva, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Reserva reserva = buscarReservaInterna(idReserva);

        if (EstadoReserva.PENDIENTE.name().equals(reserva.getEstado())) {
            throw new EstadoInvalidoException(
                    "Cancele la reserva antes de eliminarla");
        }

        reservaGateway.eliminarReserva(idReserva);
    }

    private Reserva buscarReservaInterna(String idReserva) {
        Reserva reserva = reservaGateway.buscarReservaPorId(idReserva);

        if (reserva == null) {
            throw new ReservaNoEncontradaException(
                    "Reserva no encontrada con id: " + idReserva);
        }

        return reserva;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
