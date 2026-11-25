package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaGateway {

    Reserva guardarReserva(Reserva reserva);

    Reserva actualizarReserva(Reserva reserva);

    Reserva buscarReservaPorId(String idReserva);

    List<Reserva> listarReservas();

    List<Reserva> listarReservasPorUsuario(String idUsuario);

    List<Reserva> listarReservasPorLibro(String idLibro);

    List<Reserva> listarReservasPendientesPorLibro(String idLibro);

    Reserva buscarReservaPendiente(String idUsuario, String idLibro);

    boolean existeReservaPendiente(String idUsuario, String idLibro);

    List<Reserva> listarReservasPendientesExpiradas(LocalDateTime fecha);

    void eliminarReserva(String idReserva);
}
