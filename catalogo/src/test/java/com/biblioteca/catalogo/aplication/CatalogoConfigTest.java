package com.biblioteca.catalogo.aplication;

import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import com.biblioteca.catalogo.domain.model.gateway.EjemplarGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroExternoGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CatalogoConfigTest {

    private final CatalogoConfig config = new CatalogoConfig();

    @Test
    void creaTodosLosCasosDeUsoConSusGateways() {
        AutorGateway autorGateway = mock(AutorGateway.class);
        EditorialGateway editorialGateway = mock(EditorialGateway.class);
        CategoriaGateway categoriaGateway = mock(CategoriaGateway.class);
        LibroGateway libroGateway = mock(LibroGateway.class);
        EjemplarGateway ejemplarGateway = mock(EjemplarGateway.class);
        LibroExternoGateway libroExternoGateway = mock(LibroExternoGateway.class);

        assertNotNull(config.autorUseCase(autorGateway));
        assertNotNull(config.editorialUseCase(editorialGateway));
        assertNotNull(config.categoriaUseCase(categoriaGateway));
        assertNotNull(config.libroUseCase(libroGateway));
        assertNotNull(config.ejemplarUseCase(ejemplarGateway, libroGateway));
        assertNotNull(config.catalogoExternoUseCase(
                libroExternoGateway,
                config.libroUseCase(libroGateway),
                editorialGateway,
                autorGateway,
                categoriaGateway
        ));
    }
}
