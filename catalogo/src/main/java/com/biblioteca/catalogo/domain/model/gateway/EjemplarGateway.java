package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.Ejemplar;

import java.util.List;

public interface EjemplarGateway {

    Ejemplar guardarEjemplar(Ejemplar ejemplar);

    Ejemplar actualizarEjemplar(Ejemplar ejemplar);

    Ejemplar buscarEjemplarPorId(String idEjemplar);

    Ejemplar buscarEjemplarPorCodigoInventario(String codigoInventario);

    List<Ejemplar> listarEjemplares();

    List<Ejemplar> listarEjemplaresPorLibro(String idLibro);

    List<Ejemplar> listarEjemplaresDisponibles();

    void eliminarEjemplar(String idEjemplar);
}
