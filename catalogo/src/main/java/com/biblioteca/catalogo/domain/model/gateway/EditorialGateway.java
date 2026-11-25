package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.Editorial;

import java.util.List;

public interface EditorialGateway {

    Editorial guardarEditorial(Editorial editorial);

    Editorial actualizarEditorial(Editorial editorial);

    Editorial buscarEditorialPorId(String idEditorial);

    Editorial buscarEditorialPorNombre(String nombre);

    List<Editorial> listarEditoriales();

    void eliminarEditorial(String idEditorial);
}
