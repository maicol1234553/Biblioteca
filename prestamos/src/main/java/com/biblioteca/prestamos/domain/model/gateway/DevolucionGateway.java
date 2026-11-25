package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.Devolucion;

import java.util.List;

public interface DevolucionGateway {

    Devolucion guardarDevolucion(Devolucion devolucion);

    Devolucion buscarDevolucionPorId(String idDevolucion);

    Devolucion buscarDevolucionPorPrestamo(String idPrestamo);

    List<Devolucion> listarDevoluciones();

    List<Devolucion> listarDevolucionesPorPrestamo(String idPrestamo);

    void eliminarDevolucion(String idDevolucion);
}
