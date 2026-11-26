package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.LibroExterno;
import com.biblioteca.catalogo.domain.usecase.CatalogoExternoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoExternoControllerTest {

    @Mock
    private CatalogoExternoUseCase catalogoExternoUseCase;

    @InjectMocks
    private CatalogoExternoController catalogoExternoController;



    @Test
    void buscar_ConsultaValida_RetornaLibrosExternosYEstado200() {

        String consulta = "Gabriel García Márquez";
        LibroExterno libroExterno = mock(LibroExterno.class);
        List<LibroExterno> librosEsperados = List.of(libroExterno);

        when(catalogoExternoUseCase.buscar(consulta))
                .thenReturn(librosEsperados);


        ResponseEntity<List<LibroExterno>> respuesta =
                catalogoExternoController.buscar(consulta);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(librosEsperados, respuesta.getBody());
        verify(catalogoExternoUseCase).buscar(consulta);
    }



    @Test
    void buscarPorIsbn_IsbnValido_RetornaLibroExternoYEstado200() {

        String isbn = "9780306406157";
        LibroExterno libroEsperado = mock(LibroExterno.class);

        when(catalogoExternoUseCase.buscarPorIsbn(isbn))
                .thenReturn(libroEsperado);


        ResponseEntity<LibroExterno> respuesta =
                catalogoExternoController.buscarPorIsbn(isbn);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(libroEsperado, respuesta.getBody());
        verify(catalogoExternoUseCase).buscarPorIsbn(isbn);
    }

    @Test
    void buscarPorIsbn_LibroNoEncontrado_PropagaExcepcion() {

        String isbn = "9780306406157";

        when(catalogoExternoUseCase.buscarPorIsbn(isbn))
                .thenThrow(new LibroNoEncontradoException(
                        "No se encontro informacion externa para el ISBN: " + isbn
                ));


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> catalogoExternoController.buscarPorIsbn(isbn)
        );


        assertEquals(
                "No se encontro informacion externa para el ISBN: " + isbn,
                excepcion.getMessage()
        );
        verify(catalogoExternoUseCase).buscarPorIsbn(isbn);
    }



    @Test
    void importarPorIsbn_ConCategoria_RetornaLibroYEstado201() {

        String isbn = "9780306406157";
        String idCategoria = "CAT-1";
        Libro libroImportado = mock(Libro.class);

        when(catalogoExternoUseCase.importarPorIsbn(isbn, idCategoria))
                .thenReturn(libroImportado);


        ResponseEntity<Libro> respuesta =
                catalogoExternoController.importarPorIsbn(isbn, idCategoria);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(libroImportado, respuesta.getBody());
        verify(catalogoExternoUseCase)
                .importarPorIsbn(isbn, idCategoria);
    }

    @Test
    void importarPorIsbn_SinCategoria_PasaNullYRetornaEstado201() {

        String isbn = "9780306406157";
        Libro libroImportado = mock(Libro.class);

        when(catalogoExternoUseCase.importarPorIsbn(isbn, null))
                .thenReturn(libroImportado);


        ResponseEntity<Libro> respuesta =
                catalogoExternoController.importarPorIsbn(isbn, null);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(libroImportado, respuesta.getBody());
        verify(catalogoExternoUseCase)
                .importarPorIsbn(isbn, null);
    }

    @Test
    void importarPorIsbn_LibroNoEncontrado_PropagaExcepcion() {

        String isbn = "9780306406157";
        String idCategoria = "CAT-1";

        when(catalogoExternoUseCase.importarPorIsbn(isbn, idCategoria))
                .thenThrow(new LibroNoEncontradoException(
                        "No se encontro informacion externa para el ISBN: " + isbn
                ));


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> catalogoExternoController.importarPorIsbn(
                        isbn,
                        idCategoria
                )
        );


        assertEquals(
                "No se encontro informacion externa para el ISBN: " + isbn,
                excepcion.getMessage()
        );
        verify(catalogoExternoUseCase)
                .importarPorIsbn(isbn, idCategoria);
    }
}