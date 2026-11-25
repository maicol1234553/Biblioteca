package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.Prestamo;

import java.time.LocalDateTime;
import java.util.List;

public interface PrestamoGateway {

    Prestamo guardarPrestamo(Prestamo prestamo);

    Prestamo actualizarPrestamo(Prestamo prestamo);

    Prestamo buscarPrestamoPorId(String idPrestamo);

    List<Prestamo> listarPrestamos();

    List<Prestamo> listarPrestamosActivosPorUsuario(String idUsuario);

    List<Prestamo> listarHistorialPorUsuario(String idUsuario);

    long contarPrestamosActivosPorUsuario(String idUsuario);

    boolean existePrestamoActivoPorEjemplar(String idEjemplar);

    List<Prestamo> listarPrestamosVencidos(LocalDateTime fecha);

    void eliminarPrestamo(String idPrestamo);
}
