package com.biblioteca.catalogo.domain.model;

public record BusquedaLibro(
        String texto,
        String idAutor,
        String idCategoria,
        String idEditorial,
        String idioma
) {
}
