package com.biblioteca.catalogo.domain.model.gateway;

import com.biblioteca.catalogo.domain.model.LibroExterno;

import java.util.List;
import java.util.Optional;

public interface LibroExternoGateway {

    List<LibroExterno> buscar(String consulta);

    Optional<LibroExterno> buscarPorIsbn(String isbn);
}
