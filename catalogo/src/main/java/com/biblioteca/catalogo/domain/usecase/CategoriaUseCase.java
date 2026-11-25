package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class CategoriaUseCase {

    private final CategoriaGateway categoriaGateway;

    public Categoria guardarCategoria(Categoria categoria) {
        validarCategoria(categoria);

        if (esBlanco(categoria.getIdCategoria())) {
            categoria.setIdCategoria(UUID.randomUUID().toString());
        }

        validarNombreDisponible(categoria);

        return categoriaGateway.guardarCategoria(categoria);
    }

    public Categoria actualizarCategoria(String idCategoria, Categoria categoria) {
        buscarCategoriaPorId(idCategoria);

        categoria.setIdCategoria(idCategoria);
        validarCategoria(categoria);
        validarNombreDisponible(categoria);

        return categoriaGateway.actualizarCategoria(categoria);
    }

    public Categoria buscarCategoriaPorId(String idCategoria) {
        Categoria categoria = categoriaGateway.buscarCategoriaPorId(idCategoria);

        if (categoria == null) {
            throw new CategoriaNoEncontradaException("Categoria no encontrada con id: " + idCategoria);
        }

        return categoria;
    }

    public List<Categoria> listarCategorias() {
        return categoriaGateway.listarCategorias();
    }

    public void eliminarCategoria(String idCategoria) {
        buscarCategoriaPorId(idCategoria);
        categoriaGateway.eliminarCategoria(idCategoria);
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }

        if (esBlanco(categoria.getNombre())) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio");
        }
    }

    private void validarNombreDisponible(Categoria categoria) {
        Categoria existente = categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre());

        if (existente != null && !Objects.equals(existente.getIdCategoria(), categoria.getIdCategoria())) {
            throw new RecursoDuplicadoException("Ya existe una categoria con el nombre: " + categoria.getNombre());
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
