package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.Categoria;

import java.util.List;

public interface CategoriaGateway {

    Categoria guardarCategoria(Categoria categoria);

    Categoria actualizarCategoria(Categoria categoria);

    Categoria buscarCategoriaPorId(String idCategoria);

    Categoria buscarCategoriaPorNombre(String nombre);

    List<Categoria> listarCategorias();

    void eliminarCategoria(String idCategoria);
}
