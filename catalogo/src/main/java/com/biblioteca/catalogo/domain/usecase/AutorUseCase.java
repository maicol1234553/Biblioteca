package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class AutorUseCase {

    private final AutorGateway autorGateway;

    public Autor guardarAutor(Autor autor) {
        validarAutor(autor);

        if (esBlanco(autor.getIdAutor())) {
            autor.setIdAutor(UUID.randomUUID().toString());
        }

        validarNombreDisponible(autor);

        return autorGateway.guardarAutor(autor);
    }

    public Autor actualizarAutor(String idAutor, Autor autor) {
        buscarAutorPorId(idAutor);

        autor.setIdAutor(idAutor);
        validarAutor(autor);
        validarNombreDisponible(autor);

        return autorGateway.actualizarAutor(autor);
    }

    public Autor buscarAutorPorId(String idAutor) {
        Autor autor = autorGateway.buscarAutorPorId(idAutor);

        if (autor == null) {
            throw new AutorNoEncontradoException("Autor no encontrado con id: " + idAutor);
        }

        return autor;
    }

    public List<Autor> listarAutores() {
        return autorGateway.listarAutores();
    }

    public void eliminarAutor(String idAutor) {
        buscarAutorPorId(idAutor);
        autorGateway.eliminarAutor(idAutor);
    }

    private void validarAutor(Autor autor) {
        if (autor == null) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }

        if (esBlanco(autor.getNombre())) {
            throw new IllegalArgumentException("El nombre del autor es obligatorio");
        }

        if (autor.getFechaNacimiento() != null && autor.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
    }

    private void validarNombreDisponible(Autor autor) {
        Autor existente = autorGateway.buscarAutorPorNombre(autor.getNombre());

        if (existente != null && !Objects.equals(existente.getIdAutor(), autor.getIdAutor())) {
            throw new RecursoDuplicadoException("Ya existe un autor con el nombre: " + autor.getNombre());
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
