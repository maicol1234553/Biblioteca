package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.MultaNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.model.Autorizacion;
import com.biblioteca.prestamos.domain.model.EstadoMulta;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MultaUseCase {

    private final MultaGateway multaGateway;
    private final PrestamoGateway prestamoGateway;

    public List<Multa> consultarMultasPendientesPorUsuario(String idUsuario,
                                                            UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);

        return multaGateway.listarMultasPendientesPorUsuario(idUsuario);
    }

    public List<Multa> consultarMultasPorUsuario(String idUsuario,
                                                 UsuarioExterno solicitante) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Autorizacion.exigirAcceso(solicitante, idUsuario);

        return multaGateway.listarMultasPorUsuario(idUsuario);
    }

    public List<Multa> listarMultas(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return multaGateway.listarMultas();
    }

    public Multa buscarMulta(String idMulta, UsuarioExterno solicitante) {
        Multa multa = buscarMultaInterna(idMulta);
        Prestamo prestamo = buscarPrestamoInterno(multa.getIdPrestamo());
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return multa;
    }

    public List<Multa> listarMultasPorPrestamo(String idPrestamo,
                                               UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return multaGateway.listarMultasPorPrestamo(idPrestamo);
    }

    public Multa pagarMulta(String idMulta, UsuarioExterno solicitante) {
        Multa multa = buscarMultaInterna(idMulta);
        Prestamo prestamo = buscarPrestamoInterno(multa.getIdPrestamo());
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        if (!EstadoMulta.PENDIENTE.name().equals(multa.getEstado())) {
            throw new EstadoInvalidoException("Solo se pueden pagar multas pendientes");
        }

        multa.setEstado(EstadoMulta.PAGADA.name());

        return multaGateway.actualizarMulta(multa);
    }

    public Multa anularMulta(String idMulta, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Multa multa = buscarMultaInterna(idMulta);

        if (!EstadoMulta.PENDIENTE.name().equals(multa.getEstado())) {
            throw new EstadoInvalidoException("Solo se pueden anular multas pendientes");
        }

        multa.setEstado(EstadoMulta.ANULADA.name());

        return multaGateway.actualizarMulta(multa);
    }

    public void eliminarMulta(String idMulta, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);
        Multa multa = buscarMultaInterna(idMulta);

        if (!EstadoMulta.ANULADA.name().equals(multa.getEstado())) {
            throw new EstadoInvalidoException(
                    "Solo se pueden eliminar multas anuladas");
        }

        multaGateway.eliminarMulta(idMulta);
    }

    private Multa buscarMultaInterna(String idMulta) {
        Multa multa = multaGateway.buscarMultaPorId(idMulta);

        if (multa == null) {
            throw new MultaNoEncontradaException(
                    "Multa no encontrada con id: " + idMulta);
        }

        return multa;
    }

    private Prestamo buscarPrestamoInterno(String idPrestamo) {
        Prestamo prestamo = prestamoGateway.buscarPrestamoPorId(idPrestamo);

        if (prestamo == null) {
            throw new PrestamoNoEncontradoException(
                    "Prestamo no encontrado con id: " + idPrestamo);
        }

        return prestamo;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
