package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.LimiteRenovacionesExcedidoException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.PrestamoYaDevueltoException;
import com.biblioteca.prestamos.domain.exception.RenovacionNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.RenovacionNoPermitidaException;
import com.biblioteca.prestamos.domain.model.Autorizacion;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;
import com.biblioteca.prestamos.domain.model.EstadoPrestamo;
import com.biblioteca.prestamos.domain.model.EstadoRenovacion;
import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.domain.model.PoliticaPrestamos;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.TipoEventoNotificacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.RenovacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RenovacionUseCase {

    private final PrestamoGateway prestamoGateway;
    private final ReservaGateway reservaGateway;
    private final RenovacionGateway renovacionGateway;
    private final CatalogoGateway catalogoGateway;
    private final NotificacionGateway notificacionGateway;
    private final PoliticaPrestamos politica;
    private final Clock clock;

    public Prestamo renovarPrestamo(String idPrestamo, UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());
        LocalDateTime ahora = LocalDateTime.now(clock);

        if (EstadoPrestamo.DEVUELTO.name().equals(prestamo.getEstado())) {
            throw new PrestamoYaDevueltoException("El prestamo ya fue devuelto");
        }

        if (EstadoPrestamo.CANCELADO.name().equals(prestamo.getEstado())) {
            throw new EstadoInvalidoException("No se puede renovar un prestamo cancelado");
        }

        if (EstadoPrestamo.VENCIDO.name().equals(prestamo.getEstado())
                || prestamo.getFechaVencimiento().isBefore(ahora)) {
            throw new RenovacionNoPermitidaException(
                    "No se puede renovar un prestamo vencido");
        }

        long renovaciones = renovacionGateway.contarRenovacionesPorPrestamo(idPrestamo);

        if (renovaciones >= politica.maxRenovaciones()) {
            throw new LimiteRenovacionesExcedidoException(
                    "El prestamo alcanzo el limite de " + politica.maxRenovaciones()
                            + " renovaciones");
        }

        EjemplarExterno ejemplar = catalogoGateway.buscarEjemplar(prestamo.getIdEjemplar());

        if (ejemplar == null) {
            throw new EjemplarNoEncontradoException(
                    "No se pudo obtener el ejemplar del prestamo en Catalogo");
        }

        if (ejemplar.idLibro() == null || ejemplar.idLibro().isBlank()) {
            throw new EjemplarNoEncontradoException(
                    "El ejemplar no tiene un libro asociado en Catalogo");
        }

        if (!reservaGateway.listarReservasPendientesPorLibro(ejemplar.idLibro()).isEmpty()) {
            throw new RenovacionNoPermitidaException(
                    "No se puede renovar porque existen reservas pendientes del libro");
        }

        LocalDateTime fechaAnterior = prestamo.getFechaVencimiento();
        LocalDateTime fechaNueva = fechaAnterior.plusDays(politica.diasPrestamo());

        Renovacion renovacion = new Renovacion();
        renovacion.setIdRenovacion(UUID.randomUUID().toString());
        renovacion.setIdPrestamo(idPrestamo);
        renovacion.setFechaSolicitud(ahora);
        renovacion.setFechaAnterior(fechaAnterior);
        renovacion.setFechaNueva(fechaNueva);
        renovacion.setEstado(EstadoRenovacion.APROBADA.name());

        prestamo.setFechaVencimiento(fechaNueva);
        prestamo.setEstado(EstadoPrestamo.ACTIVO.name());
        prestamoGateway.actualizarPrestamo(prestamo);
        renovacionGateway.guardarRenovacion(renovacion);

        notificacionGateway.enviarEvento(new EventoNotificacion(
                TipoEventoNotificacion.RENOVACION_REGISTRADA,
                prestamo.getIdUsuario(),
                idPrestamo,
                "Prestamo renovado hasta " + fechaNueva,
                ahora
        ));

        return prestamo;
    }

    public List<Renovacion> listarRenovacionesPorPrestamo(String idPrestamo,
                                                          UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return renovacionGateway.listarRenovacionesPorPrestamo(idPrestamo);
    }

    public List<Renovacion> listarRenovaciones(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return renovacionGateway.listarRenovaciones();
    }

    public Renovacion buscarRenovacion(String idRenovacion, UsuarioExterno solicitante) {
        Renovacion renovacion = renovacionGateway.buscarRenovacionPorId(idRenovacion);

        if (renovacion == null) {
            throw new RenovacionNoEncontradaException(
                    "Renovacion no encontrada con id: " + idRenovacion);
        }

        Prestamo prestamo = buscarPrestamoInterno(renovacion.getIdPrestamo());
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return renovacion;
    }

    public void eliminarRenovacion(String idRenovacion, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        if (renovacionGateway.buscarRenovacionPorId(idRenovacion) == null) {
            throw new RenovacionNoEncontradaException(
                    "Renovacion no encontrada con id: " + idRenovacion);
        }

        renovacionGateway.eliminarRenovacion(idRenovacion);
    }

    private Prestamo buscarPrestamoInterno(String idPrestamo) {
        Prestamo prestamo = prestamoGateway.buscarPrestamoPorId(idPrestamo);

        if (prestamo == null) {
            throw new PrestamoNoEncontradoException(
                    "Prestamo no encontrado con id: " + idPrestamo);
        }

        return prestamo;
    }
}
