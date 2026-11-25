package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.Multa;

import java.util.List;

public interface MultaGateway {

    Multa guardarMulta(Multa multa);

    Multa actualizarMulta(Multa multa);

    Multa buscarMultaPorId(String idMulta);

    List<Multa> listarMultas();

    List<Multa> listarMultasPorPrestamo(String idPrestamo);

    List<Multa> listarMultasPorUsuario(String idUsuario);

    List<Multa> listarMultasPendientesPorUsuario(String idUsuario);

    Multa buscarMultaPendientePorPrestamo(String idPrestamo);

    void eliminarMulta(String idMulta);
}
