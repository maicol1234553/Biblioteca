package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import com.biblioteca.catalogo.infraestructure.mapper.LibroMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LibroDataGatewayImpl implements LibroGateway {

    private final LibroDataJpaRepository libroDataJpaRepository;
    private final EditorialDataJpaRepository editorialDataJpaRepository;
    private final CategoriaDataJpaRepository categoriaDataJpaRepository;
    private final AutorDataJpaRepository autorDataJpaRepository;
    private final LibroMapper libroMapper;

    @Override
    @Transactional
    public Libro guardarLibro(Libro libro) {
        return libroMapper.toLibro(libroDataJpaRepository.save(construirLibroData(libro)));
    }

    @Override
    @Transactional
    public Libro actualizarLibro(Libro libro) {
        return libroMapper.toLibro(libroDataJpaRepository.save(construirLibroData(libro)));
    }

    @Override
    public Libro buscarLibroPorId(String idLibro) {
        return libroDataJpaRepository.findById(idLibro)
                .map(libroMapper::toLibro)
                .orElse(null);
    }

    @Override
    public Libro buscarLibroPorIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }

        return libroDataJpaRepository.findByIsbn(isbn)
                .map(libroMapper::toLibro)
                .orElse(null);
    }

    @Override
    public List<Libro> buscarLibros(BusquedaLibro busqueda) {
        BusquedaLibro filtro = busqueda == null
                ? new BusquedaLibro(null, null, null, null, null)
                : busqueda;

        return libroDataJpaRepository.buscar(
                        limpiar(filtro.texto()),
                        limpiar(filtro.idAutor()),
                        limpiar(filtro.idCategoria()),
                        limpiar(filtro.idEditorial()),
                        limpiar(filtro.idioma()))
                .stream()
                .map(libroMapper::toLibro)
                .toList();
    }

    @Override
    public List<Libro> listarLibros() {
        return libroDataJpaRepository.findAllByOrderByTituloAsc()
                .stream()
                .map(libroMapper::toLibro)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarLibro(String idLibro) {
        try {
            libroDataJpaRepository.deleteById(idLibro);
            libroDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar el libro porque tiene ejemplares registrados");
        }
    }

    private LibroData construirLibroData(Libro libro) {
        LibroData libroData = libroMapper.toLibroData(libro);

        EditorialData editorial = editorialDataJpaRepository.findById(libro.getIdEditorial())
                .orElseThrow(() -> new EditorialNoEncontradaException(
                        "Editorial no encontrada con id: " + libro.getIdEditorial()));
        libroData.setEditorial(editorial);

        Set<CategoriaData> categorias = new LinkedHashSet<>();
        for (Categoria categoria : libro.getCategorias()) {
            categorias.add(categoriaDataJpaRepository.findById(categoria.getIdCategoria())
                    .orElseThrow(() -> new CategoriaNoEncontradaException(
                            "Categoria no encontrada con id: " + categoria.getIdCategoria())));
        }
        libroData.setCategorias(categorias);

        Set<AutorData> autores = new LinkedHashSet<>();
        for (Autor autor : libro.getAutores()) {
            autores.add(autorDataJpaRepository.findById(autor.getIdAutor())
                    .orElseThrow(() -> new AutorNoEncontradoException(
                            "Autor no encontrado con id: " + autor.getIdAutor())));
        }
        libroData.setAutores(autores);

        if (libroData.getFechaRegistro() == null) {
            libroData.setFechaRegistro(LocalDateTime.now());
        }

        return libroData;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
