package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.EjemplarNoDisponibleException;
import com.biblioteca.prestamos.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LimitePrestamosExcedidoException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.biblioteca.prestamos.domain.model.Autorizacion;
import com.biblioteca.prestamos.domain.model.CalculadoraMulta;
import com.biblioteca.prestamos.domain.model.CondicionDevolucion;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;
import com.biblioteca.prestamos.domain.model.EstadoMulta;
import com.biblioteca.prestamos.domain.model.EstadoPrestamo;
import com.biblioteca.prestamos.domain.model.EstadoReserva;
import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.PoliticaPrestamos;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PrestamoUseCase {

    private static final Logger log = LoggerFactory.getLogger(PrestamoUseCase.class);

    private final PrestamoGateway prestamoGateway;
    private final ReservaGateway reservaGateway;
    private final MultaGateway multaGateway;
    private final DevolucionGateway devolucionGateway;
    private final AuthGateway authGateway;
    private final CatalogoGateway catalogoGateway;
    private final NotificacionGateway notificacionGateway;
    private final PoliticaPrestamos politica;
    private final Clock clock;

    public Prestamo solicitarPrestamo(String idUsuario,
                                      String idEjemplar,
                                      UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (esBlanco(idEjemplar)) {
            throw new IllegalArgumentException("El ejemplar es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);
        UsuarioExterno usuario = ValidadorUsuario.validar(authGateway, solicitante, idUsuario);

        EjemplarExterno ejemplar = catalogoGateway.buscarEjemplar(idEjemplar);

        if (ejemplar == null) {
            throw new EjemplarNoEncontradoException("Ejemplar no encontrado con id: " + idEjemplar);
        }

        if (!ejemplar.estaDisponible()) {
            throw new EjemplarNoDisponibleException(
                    "El ejemplar no esta disponible para prestamo. Estado actual: " + ejemplar.estado());
        }

        if (prestamoGateway.existePrestamoActivoPorEjemplar(idEjemplar)) {
            throw new EjemplarNoDisponibleException(
                    "El ejemplar ya tiene un prestamo activo registrado");
        }

        long prestamosActivos = prestamoGateway.contarPrestamosActivosPorUsuario(idUsuario);

        if (prestamosActivos >= politica.limitePrestamosActivos()) {
            throw new LimitePrestamosExcedidoException(
                    "El usuario alcanzo el limite de " + politica.limitePrestamosActivos()
                            + " prestamos activos");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(UUID.randomUUID().toString());
        prestamo.setIdUsuario(usuario.idUsuario());
        prestamo.setIdEjemplar(idEjemplar);
        prestamo.setFechaPrestamo(ahora);
        prestamo.setFechaVencimiento(ahora.plusDays(politica.diasPrestamo()));
        prestamo.setEstado(EstadoPrestamo.ACTIVO.name());

        catalogoGateway.actualizarEstadoEjemplar(idEjemplar, "PRESTADO");

        Prestamo guardado;

        try {
            guardado = prestamoGateway.guardarPrestamo(prestamo);
        } catch (RuntimeException exception) {
            revertirEstadoEjemplar(idEjemplar, "DISPONIBLE");
            throw exception;
        }

        convertirReservaPendiente(idUsuario, ejemplar.idLibro(), ahora);

        notificar(
                TipoEventoNotificacion.PRESTAMO_REGISTRADO,
                idUsuario,
                guardado.getIdPrestamo(),
                "Prestamo registrado. Fecha de vencimiento: " + guardado.getFechaVencimiento(),
                ahora
        );

        return guardado;
    }

    public Devolucion devolverPrestamo(String idPrestamo,
                                       String estadoEjemplar,
                                       String observaciones,
                                       UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        if (EstadoPrestamo.DEVUELTO.name().equals(prestamo.getEstado())) {
            throw new PrestamoYaDevueltoException(
                    "El prestamo ya fue devuelto con id: " + idPrestamo);
        }

        if (devolucionGateway.buscarDevolucionPorPrestamo(idPrestamo) != null) {
            throw new PrestamoYaDevueltoException(
                    "El prestamo ya tiene una devolucion registrada con id: " + idPrestamo);
        }

        if (EstadoPrestamo.CANCELADO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException("No se puede devolver un prestamo cancelado");
        }

        CondicionDevolucion condicion = CondicionDevolucion.desde(estadoEjemplar);
        LocalDateTime ahora = LocalDateTime.now(clock);
        long diasRetraso = CalculadoraMulta.diasDeRetraso(prestamo.getFechaVencimiento(), ahora);

        Devolucion devolucion = new Devolucion();
        devolucion.setIdDevolucion(UUID.randomUUID().toString());
        devolucion.setIdPrestamo(idPrestamo);
        devolucion.setFechaDevolucion(ahora);
        devolucion.setObservaciones(observaciones);
        devolucion.setEstadoEjemplar(condicion.name());

        Devolucion guardada;

        try {
            catalogoGateway.actualizarEstadoEjemplar(
                    prestamo.getIdEjemplar(), condicion.estadoCatalogo());
            actualizarMulta(idPrestamo, diasRetraso, ahora);

            prestamo.setEstado(EstadoPrestamo.DEVUELTO.name());
            prestamo.setFechaDevolucion(ahora);
            prestamoGateway.actualizarPrestamo(prestamo);

            guardada = devolucionGateway.guardarDevolucion(devolucion);
        } catch (RuntimeException exception) {
            revertirEstadoEjemplar(prestamo.getIdEjemplar(), "PRESTADO");
            throw exception;
        }

        notificar(
                TipoEventoNotificacion.DEVOLUCION_REGISTRADA,
                prestamo.getIdUsuario(),
                idPrestamo,
                "Devolucion registrada. Condicion del ejemplar: " + condicion.name(),
                ahora
        );

        return guardada;
    }

    public List<Prestamo> consultarMisPrestamos(String idUsuario, UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);

        return prestamoGateway.listarPrestamosActivosPorUsuario(idUsuario);
    }

    public List<Prestamo> consultarHistorial(String idUsuario, UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);

        return prestamoGateway.listarHistorialPorUsuario(idUsuario);
    }

    public List<Prestamo> consultarPrestamosVencidos(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return procesarPrestamosVencidos();
    }





    public List<Prestamo> procesarPrestamosVencidos() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Prestamo> vencidos = prestamoGateway.listarPrestamosVencidos(ahora);

        for (Prestamo prestamo : vencidos) {
            prestamo.setEstado(EstadoPrestamo.VENCIDO.name());
            prestamoGateway.actualizarPrestamo(prestamo);

            long diasRetraso = CalculadoraMulta.diasDeRetraso(
                    prestamo.getFechaVencimiento(), ahora);
            actualizarMulta(prestamo.getIdPrestamo(), diasRetraso, ahora);

            notificar(
                    TipoEventoNotificacion.PRESTAMO_VENCIDO,
                    prestamo.getIdUsuario(),
                    prestamo.getIdPrestamo(),
                    "El prestamo esta vencido. Multa acumulada por " + diasRetraso + " dia(s)",
                    ahora
            );
        }

        return vencidos;
    }

    public Prestamo buscarPrestamoPorId(String idPrestamo, UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return prestamo;
    }

    public List<Prestamo> listarPrestamos(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return prestamoGateway.listarPrestamos();
    }

    public Prestamo actualizarVencimiento(String idPrestamo,
                                          LocalDateTime nuevaFecha,
                                          UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);

        if (!EstadoPrestamo.ACTIVO.name().equals(prestamo.getEstado())
                && !EstadoPrestamo.VENCIDO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException(
                    "Solo se puede modificar el vencimiento de un prestamo activo o vencido");
        }

        if (nuevaFecha == null || !nuevaFecha.isAfter(prestamo.getFechaPrestamo())) {
            throw new IllegalArgumentException(
                    "La nueva fecha de vencimiento debe ser posterior a la fecha del prestamo");
        }

        prestamo.setFechaVencimiento(nuevaFecha);
        prestamo.setEstado(nuevaFecha.isAfter(LocalDateTime.now(clock))
                ? EstadoPrestamo.ACTIVO.name()
                : EstadoPrestamo.VENCIDO.name());

        return prestamoGateway.actualizarPrestamo(prestamo);
    }

    public Prestamo cancelarPrestamo(String idPrestamo, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);

        if (EstadoPrestamo.DEVUELTO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException("No se puede cancelar un prestamo devuelto");
        }

        if (EstadoPrestamo.CANCELADO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException("El prestamo ya esta cancelado");
        }

        catalogoGateway.actualizarEstadoEjemplar(prestamo.getIdEjemplar(), "DISPONIBLE");
        prestamo.setEstado(EstadoPrestamo.CANCELADO.name());

        try {
            return prestamoGateway.actualizarPrestamo(prestamo);
        } catch (RuntimeException exception) {
            revertirEstadoEjemplar(prestamo.getIdEjemplar(), "PRESTADO");
            throw exception;
        }
    }

    public void eliminarPrestamo(String idPrestamo, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);

        if (!EstadoPrestamo.DEVUELTO.name().equals(prestamo.getEstado())
                && !EstadoPrestamo.CANCELADO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException(
                    "Solo se pueden eliminar prestamos devueltos o cancelados");
        }

        prestamoGateway.eliminarPrestamo(idPrestamo);
    }

    private Prestamo buscarPrestamoInterno(String idPrestamo) {
        Prestamo prestamo = prestamoGateway.buscarPrestamoPorId(idPrestamo);

        if (prestamo == null) {
            throw new PrestamoNoEncontradoException(
                    "Prestamo no encontrado con id: " + idPrestamo);
        }

        return prestamo;
    }

    private void actualizarMulta(String idPrestamo, long diasRetraso, LocalDateTime fecha) {
        if (diasRetraso <= 0) {
            return;
        }

        Multa multa = multaGateway.buscarMultaPendientePorPrestamo(idPrestamo);
        boolean nueva = multa == null;

        if (nueva) {
            multa = new Multa();
            multa.setIdMulta(UUID.randomUUID().toString());
            multa.setIdPrestamo(idPrestamo);
            multa.setEstado(EstadoMulta.PENDIENTE.name());
        }

        multa.setValor(CalculadoraMulta.valorMulta(diasRetraso, politica.multaPorDia()));
        multa.setMotivo("Multa por " + diasRetraso + " dia(s) de retraso");
        multa.setFechaGeneracion(fecha);

        if (nueva) {
            multaGateway.guardarMulta(multa);
        } else {
            multaGateway.actualizarMulta(multa);
        }
    }

    private void convertirReservaPendiente(String idUsuario, String idLibro, LocalDateTime fecha) {
        if (esBlanco(idLibro)) {
            return;
        }

        try {
            Reserva reserva = reservaGateway.buscarReservaPendiente(idUsuario, idLibro);

            if (reserva != null) {
                reserva.setEstado(EstadoReserva.CONVERTIDA.name());
                reservaGateway.actualizarReserva(reserva);
            }
        } catch (RuntimeException exception) {
            log.warn("No se pudo convertir la reserva del usuario {} para el libro {}: {}",
                    idUsuario, idLibro, exception.getMessage());
        }
    }

    private void revertirEstadoEjemplar(String idEjemplar, String estado) {
        try {
            catalogoGateway.actualizarEstadoEjemplar(idEjemplar, estado);
        } catch (RuntimeException exception) {
            log.warn("No se pudo revertir el estado del ejemplar {} a {}: {}",
                    idEjemplar, estado, exception.getMessage());
        }
    }

    private void notificar(TipoEventoNotificacion tipo,
                           String idUsuario,
                           String referenciaId,
                           String mensaje,
                           LocalDateTime fecha) {
        notificacionGateway.enviarEvento(
                new EventoNotificacion(tipo, idUsuario, referenciaId, mensaje, fecha));
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
