package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class EditorialUseCase {

    private final EditorialGateway editorialGateway;

    public Editorial guardarEditorial(Editorial editorial) {
        validarEditorial(editorial);

        if (esBlanco(editorial.getIdEditorial())) {
            editorial.setIdEditorial(UUID.randomUUID().toString());
        }

        validarNombreDisponible(editorial);

        return editorialGateway.guardarEditorial(editorial);
    }

    public Editorial actualizarEditorial(String idEditorial, Editorial editorial) {
        buscarEditorialPorId(idEditorial);

        editorial.setIdEditorial(idEditorial);
        validarEditorial(editorial);
        validarNombreDisponible(editorial);

        return editorialGateway.actualizarEditorial(editorial);
    }

    public Editorial buscarEditorialPorId(String idEditorial) {
        Editorial editorial = editorialGateway.buscarEditorialPorId(idEditorial);

        if (editorial == null) {
            throw new EditorialNoEncontradaException("Editorial no encontrada con id: " + idEditorial);
        }

        return editorial;
    }

    public List<Editorial> listarEditoriales() {
        return editorialGateway.listarEditoriales();
    }

    public void eliminarEditorial(String idEditorial) {
        buscarEditorialPorId(idEditorial);
        editorialGateway.eliminarEditorial(idEditorial);
    }

    private void validarEditorial(Editorial editorial) {
        if (editorial == null) {
            throw new IllegalArgumentException("La editorial es obligatoria");
        }

        if (esBlanco(editorial.getNombre())) {
            throw new IllegalArgumentException("El nombre de la editorial es obligatorio");
        }

        if (!esBlanco(editorial.getCorreoContacto()) && !editorial.getCorreoContacto().contains("@")) {
            throw new IllegalArgumentException("El correo de contacto no es valido");
        }

        if (!esBlanco(editorial.getSitioWeb())
                && !editorial.getSitioWeb().startsWith("http://")
                && !editorial.getSitioWeb().startsWith("https://")) {
            throw new IllegalArgumentException("El sitio web debe iniciar con http:// o https://");
        }
    }

    private void validarNombreDisponible(Editorial editorial) {
        Editorial existente = editorialGateway.buscarEditorialPorNombre(editorial.getNombre());

        if (existente != null && !Objects.equals(existente.getIdEditorial(), editorial.getIdEditorial())) {
            throw new RecursoDuplicadoException("Ya existe una editorial con el nombre: " + editorial.getNombre());
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
