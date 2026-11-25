package com.biblioteca.catalogo.aplication;

import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import com.biblioteca.catalogo.domain.model.gateway.EjemplarGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroExternoGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import com.biblioteca.catalogo.domain.usecase.AutorUseCase;
import com.biblioteca.catalogo.domain.usecase.CatalogoExternoUseCase;
import com.biblioteca.catalogo.domain.usecase.CategoriaUseCase;
import com.biblioteca.catalogo.domain.usecase.EditorialUseCase;
import com.biblioteca.catalogo.domain.usecase.EjemplarUseCase;
import com.biblioteca.catalogo.domain.usecase.LibroUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogoConfig {

    @Bean
    public AutorUseCase autorUseCase(AutorGateway autorGateway) {
        return new AutorUseCase(autorGateway);
    }

    @Bean
    public EditorialUseCase editorialUseCase(EditorialGateway editorialGateway) {
        return new EditorialUseCase(editorialGateway);
    }

    @Bean
    public CategoriaUseCase categoriaUseCase(CategoriaGateway categoriaGateway) {
        return new CategoriaUseCase(categoriaGateway);
    }

    @Bean
    public LibroUseCase libroUseCase(LibroGateway libroGateway) {
        return new LibroUseCase(libroGateway);
    }

    @Bean
    public EjemplarUseCase ejemplarUseCase(EjemplarGateway ejemplarGateway, LibroGateway libroGateway) {
        return new EjemplarUseCase(ejemplarGateway, libroGateway);
    }

    @Bean
    public CatalogoExternoUseCase catalogoExternoUseCase(LibroExternoGateway libroExternoGateway,
                                                         LibroUseCase libroUseCase,
                                                         EditorialGateway editorialGateway,
                                                         AutorGateway autorGateway,
                                                         CategoriaGateway categoriaGateway) {
        return new CatalogoExternoUseCase(
                libroExternoGateway,
                libroUseCase,
                editorialGateway,
                autorGateway,
                categoriaGateway
        );
    }
}
