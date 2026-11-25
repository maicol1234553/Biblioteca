package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.Renovacion;

import java.util.List;

public interface RenovacionGateway {

    Renovacion guardarRenovacion(Renovacion renovacion);

    Renovacion buscarRenovacionPorId(String idRenovacion);

    List<Renovacion> listarRenovaciones();

    List<Renovacion> listarRenovacionesPorPrestamo(String idPrestamo);

    long contarRenovacionesPorPrestamo(String idPrestamo);

    void eliminarRenovacion(String idRenovacion);
}
