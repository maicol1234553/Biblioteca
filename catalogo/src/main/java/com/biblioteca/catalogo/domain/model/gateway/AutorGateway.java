package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.Autor;

import java.util.List;

public interface AutorGateway {

    Autor guardarAutor(Autor autor);

    Autor actualizarAutor(Autor autor);

    Autor buscarAutorPorId(String idAutor);

    Autor buscarAutorPorNombre(String nombre);

    List<Autor> listarAutores();

    void eliminarAutor(String idAutor);
}
