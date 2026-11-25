package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Libro;

import java.util.List;

public interface LibroGateway {

    Libro guardarLibro(Libro libro);

    Libro actualizarLibro(Libro libro);

    Libro buscarLibroPorId(String idLibro);

    Libro buscarLibroPorIsbn(String isbn);

    List<Libro> buscarLibros(BusquedaLibro busqueda);

    List<Libro> listarLibros();

    void eliminarLibro(String idLibro);
}
