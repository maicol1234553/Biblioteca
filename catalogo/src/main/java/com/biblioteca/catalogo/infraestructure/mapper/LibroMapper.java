package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.infraestructure.driver_adapters.LibroData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LibroMapper {

    private final AutorMapper autorMapper;
    private final CategoriaMapper categoriaMapper;

    public Libro toLibro(LibroData libroData) {
        if (libroData == null) {
            return null;
        }

        List<Categoria> categorias = libroData.getCategorias() == null
                ? new ArrayList<>()
                : libroData.getCategorias().stream()
                        .map(categoriaMapper::toCategoria)
                        .toList();

        List<Autor> autores = libroData.getAutores() == null
                ? new ArrayList<>()
                : libroData.getAutores().stream()
                        .map(autorMapper::toAutor)
                        .toList();

        return new Libro(
                libroData.getIdLibro(),
                libroData.getIsbn(),
                libroData.getTitulo(),
                libroData.getSubtitulo(),
                libroData.getFechaPublicacion(),
                libroData.getIdioma(),
                libroData.getNumeroPaginas(),
                libroData.getDescripcion(),
                libroData.getPortadaUrl(),
                libroData.getEditorial() == null ? null : libroData.getEditorial().getIdEditorial(),
                libroData.getEditorial() == null ? null : libroData.getEditorial().getNombre(),
                new ArrayList<>(categorias),
                new ArrayList<>(autores),
                libroData.getFechaRegistro()
        );
    }

    /**
     * Mapea solo los campos escalares. Las relaciones (editorial, categorias y
     * autores) las resuelve el gateway contra la base de datos.
     */
    public LibroData toLibroData(Libro libro) {
        LibroData libroData = new LibroData();

        libroData.setIdLibro(libro.getIdLibro());
        libroData.setIsbn(libro.getIsbn());
        libroData.setTitulo(libro.getTitulo());
        libroData.setSubtitulo(libro.getSubtitulo());
        libroData.setFechaPublicacion(libro.getFechaPublicacion());
        libroData.setIdioma(libro.getIdioma());
        libroData.setNumeroPaginas(libro.getNumeroPaginas());
        libroData.setDescripcion(libro.getDescripcion());
        libroData.setPortadaUrl(libro.getPortadaUrl());
        libroData.setFechaRegistro(libro.getFechaRegistro());

        return libroData;
    }
}
