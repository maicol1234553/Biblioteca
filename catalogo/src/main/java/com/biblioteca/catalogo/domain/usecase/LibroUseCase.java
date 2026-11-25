package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class LibroUseCase {

    private final LibroGateway libroGateway;

    public Libro guardarLibro(Libro libro) {
        validarLibro(libro);

        if (esBlanco(libro.getIdLibro())) {
            libro.setIdLibro(UUID.randomUUID().toString());
        }

        if (libro.getFechaRegistro() == null) {
            libro.setFechaRegistro(LocalDateTime.now());
        }

        validarIsbnDisponible(libro);

        return libroGateway.guardarLibro(libro);
    }

    public Libro actualizarLibro(String idLibro, Libro libro) {
        Libro existente = buscarLibroPorId(idLibro);

        libro.setIdLibro(idLibro);

        if (libro.getSubtitulo() == null) {
            libro.setSubtitulo(existente.getSubtitulo());
        }
        if (libro.getFechaPublicacion() == null) {
            libro.setFechaPublicacion(existente.getFechaPublicacion());
        }
        if (libro.getIdioma() == null) {
            libro.setIdioma(existente.getIdioma());
        }
        if (libro.getNumeroPaginas() == null) {
            libro.setNumeroPaginas(existente.getNumeroPaginas());
        }
        if (libro.getDescripcion() == null) {
            libro.setDescripcion(existente.getDescripcion());
        }
        if (libro.getPortadaUrl() == null) {
            libro.setPortadaUrl(existente.getPortadaUrl());
        }
        if (esBlanco(libro.getIsbn())) {
            libro.setIsbn(existente.getIsbn());
        }
        if (esBlanco(libro.getIdEditorial())) {
            libro.setIdEditorial(existente.getIdEditorial());
        }
        if (libro.getCategorias() == null || libro.getCategorias().isEmpty()) {
            libro.setCategorias(existente.getCategorias());
        }
        if (libro.getAutores() == null || libro.getAutores().isEmpty()) {
            libro.setAutores(existente.getAutores());
        }
        if (libro.getFechaRegistro() == null) {
            libro.setFechaRegistro(existente.getFechaRegistro());
        }

        libro.setNombreEditorial(null);
        validarLibro(libro);
        validarIsbnDisponible(libro);

        return libroGateway.actualizarLibro(libro);
    }

    public Libro buscarLibroPorId(String idLibro) {
        Libro libro = libroGateway.buscarLibroPorId(idLibro);

        if (libro == null) {
            throw new LibroNoEncontradoException("Libro no encontrado con id: " + idLibro);
        }

        return libro;
    }

    public Libro buscarLibroPorIsbn(String isbn) {
        return libroGateway.buscarLibroPorIsbn(isbn);
    }

    public List<Libro> listarLibros() {
        return libroGateway.listarLibros();
    }

    public List<Libro> buscarLibros(BusquedaLibro busqueda) {
        if (busqueda == null) {
            return libroGateway.listarLibros();
        }

        return libroGateway.buscarLibros(new BusquedaLibro(
                limpiar(busqueda.texto()),
                limpiar(busqueda.idAutor()),
                limpiar(busqueda.idCategoria()),
                limpiar(busqueda.idEditorial()),
                limpiar(busqueda.idioma())
        ));
    }

    public void eliminarLibro(String idLibro) {
        buscarLibroPorId(idLibro);
        libroGateway.eliminarLibro(idLibro);
    }

    private void validarLibro(Libro libro) {
        if (libro == null) {
            throw new IllegalArgumentException("El libro es obligatorio");
        }

        if (esBlanco(libro.getTitulo())) {
            throw new IllegalArgumentException("El titulo del libro es obligatorio");
        }

        if (esBlanco(libro.getIdEditorial())) {
            throw new IllegalArgumentException("La editorial del libro es obligatoria");
        }

        if (libro.getCategorias() == null || libro.getCategorias().isEmpty()) {
            throw new IllegalArgumentException("Debe clasificar el libro en al menos una categoria");
        }

        if (libro.getNumeroPaginas() != null && libro.getNumeroPaginas() <= 0) {
            throw new IllegalArgumentException("El numero de paginas debe ser mayor a cero");
        }

        if (!esBlanco(libro.getIsbn()) && !libro.getIsbn().matches("[0-9Xx-]{10,17}")) {
            throw new IllegalArgumentException("El ISBN no tiene un formato valido");
        }

        if (libro.getAutores() == null) {
            libro.setAutores(new ArrayList<>());
        }

        if (libro.getCategorias() == null) {
            libro.setCategorias(new ArrayList<>());
        }
    }

    private void validarIsbnDisponible(Libro libro) {
        if (esBlanco(libro.getIsbn())) {
            return;
        }

        Libro existente = libroGateway.buscarLibroPorIsbn(libro.getIsbn());

        if (existente != null && !Objects.equals(existente.getIdLibro(), libro.getIdLibro())) {
            throw new RecursoDuplicadoException("Ya existe un libro con el ISBN: " + libro.getIsbn());
        }
    }

    private String limpiar(String valor) {
        return esBlanco(valor) ? null : valor.trim();
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
