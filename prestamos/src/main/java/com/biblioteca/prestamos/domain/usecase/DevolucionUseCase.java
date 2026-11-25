package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.DevolucionNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.model.Autorizacion;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DevolucionUseCase {

    private final DevolucionGateway devolucionGateway;
    private final PrestamoGateway prestamoGateway;

    public List<Devolucion> listarDevoluciones(UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        return devolucionGateway.listarDevoluciones();
    }

    public Devolucion buscarDevolucion(String idDevolucion, UsuarioExterno solicitante) {
        Devolucion devolucion = devolucionGateway.buscarDevolucionPorId(idDevolucion);

        if (devolucion == null) {
            throw new DevolucionNoEncontradaException(
                    "Devolucion no encontrada con id: " + idDevolucion);
        }

        Prestamo prestamo = buscarPrestamoInterno(devolucion.getIdPrestamo());
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return devolucion;
    }

    public List<Devolucion> listarDevolucionesPorPrestamo(String idPrestamo,
                                                          UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        return devolucionGateway.listarDevolucionesPorPrestamo(idPrestamo);
    }

    public Devolucion buscarDevolucionPorPrestamo(String idPrestamo,
                                                  UsuarioExterno solicitante) {
        Prestamo prestamo = buscarPrestamoInterno(idPrestamo);
        Autorizacion.exigirAcceso(solicitante, prestamo.getIdUsuario());

        Devolucion devolucion = devolucionGateway.buscarDevolucionPorPrestamo(idPrestamo);

        if (devolucion == null) {
            throw new DevolucionNoEncontradaException(
                    "El prestamo no tiene una devolucion registrada con id: " + idPrestamo);
        }

        return devolucion;
    }

    public void eliminarDevolucion(String idDevolucion, UsuarioExterno solicitante) {
        Autorizacion.exigirAdministrador(solicitante);

        if (devolucionGateway.buscarDevolucionPorId(idDevolucion) == null) {
            throw new DevolucionNoEncontradaException(
                    "Devolucion no encontrada con id: " + idDevolucion);
        }

        devolucionGateway.eliminarDevolucion(idDevolucion);
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
