package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.LibroExterno;
import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroExternoGateway;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CatalogoExternoUseCase {

    private static final String ISBN_PATTERN = "[0-9Xx-]{10,17}";

    private final LibroExternoGateway libroExternoGateway;
    private final LibroUseCase libroUseCase;
    private final EditorialGateway editorialGateway;
    private final AutorGateway autorGateway;
    private final CategoriaGateway categoriaGateway;

    public List<LibroExterno> buscar(String consulta) {
        if (esBlanco(consulta)) {
            throw new IllegalArgumentException("El termino de busqueda es obligatorio");
        }

        return libroExternoGateway.buscar(consulta.trim());
    }

    public LibroExterno buscarPorIsbn(String isbn) {
        String isbnNormalizado = normalizarIsbn(isbn);

        return libroExternoGateway.buscarPorIsbn(isbnNormalizado)
                .orElseThrow(() -> new LibroNoEncontradoException(
                        "No se encontro informacion externa para el ISBN: " + isbnNormalizado));
    }

    public Libro importarPorIsbn(String isbn, String idCategoria) {
        String isbnNormalizado = normalizarIsbn(isbn);

        LibroExterno externo = libroExternoGateway.buscarPorIsbn(isbnNormalizado)
                .orElseThrow(() -> new LibroNoEncontradoException(
                        "No se encontro informacion externa para el ISBN: " + isbnNormalizado));

        Editorial editorial = resolverEditorial(externo.getEditorial());
        Categoria categoria = resolverCategoria(idCategoria);
        List<Autor> autores = resolverAutores(externo.getAutores());

        Libro libro = new Libro();
        libro.setIsbn(isbnNormalizado);
        libro.setTitulo(externo.getTitulo());
        libro.setSubtitulo(externo.getSubtitulo());
        libro.setFechaPublicacion(externo.getFechaPublicacion());
        libro.setIdioma(externo.getIdioma());
        libro.setNumeroPaginas(externo.getNumeroPaginas());
        libro.setDescripcion(externo.getDescripcion());
        libro.setPortadaUrl(externo.getPortadaUrl());
        libro.setIdEditorial(editorial.getIdEditorial());
        libro.setNombreEditorial(editorial.getNombre());
        libro.setCategorias(new ArrayList<>(List.of(categoria)));
        libro.setAutores(new ArrayList<>(autores));

        return libroUseCase.guardarLibro(libro);
    }

    private Editorial resolverEditorial(String nombre) {
        String nombreFinal = esBlanco(nombre) ? "Sin editorial" : nombre.trim();

        Editorial editorial = editorialGateway.buscarEditorialPorNombre(nombreFinal);

        if (editorial != null) {
            return editorial;
        }

        editorial = new Editorial();
        editorial.setIdEditorial(UUID.randomUUID().toString());
        editorial.setNombre(nombreFinal);

        return editorialGateway.guardarEditorial(editorial);
    }

    private List<Autor> resolverAutores(List<String> nombres) {
        List<Autor> autores = new ArrayList<>();

        if (nombres == null) {
            return autores;
        }

        for (String nombre : nombres) {
            if (esBlanco(nombre)) {
                continue;
            }

            String nombreFinal = nombre.trim();
            Autor autor = autorGateway.buscarAutorPorNombre(nombreFinal);

            if (autor == null) {
                autor = new Autor();
                autor.setIdAutor(UUID.randomUUID().toString());
                autor.setNombre(nombreFinal);
                autor = autorGateway.guardarAutor(autor);
            }

            autores.add(autor);
        }

        return autores;
    }

    private Categoria resolverCategoria(String idCategoria) {
        if (!esBlanco(idCategoria)) {
            Categoria categoria = categoriaGateway.buscarCategoriaPorId(idCategoria);

            if (categoria == null) {
                throw new CategoriaNoEncontradaException(
                        "Categoria no encontrada con id: " + idCategoria);
            }

            return categoria;
        }

        Categoria general = categoriaGateway.buscarCategoriaPorNombre("General");

        if (general != null) {
            return general;
        }

        general = new Categoria();
        general.setIdCategoria(UUID.randomUUID().toString());
        general.setNombre("General");
        general.setDescripcion("Categoria asignada automaticamente al importar libros externos");

        return categoriaGateway.guardarCategoria(general);
    }

    private String normalizarIsbn(String isbn) {
        if (esBlanco(isbn)) {
            throw new IllegalArgumentException("El ISBN es obligatorio");
        }

        String limpio = isbn.trim();

        if (!limpio.matches(ISBN_PATTERN)) {
            throw new IllegalArgumentException("El ISBN no tiene un formato valido");
        }

        return limpio.replaceAll("[^0-9Xx]", "");
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
