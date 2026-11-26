package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.usecase.AutorUseCase;
import org.junit.jupiter.api.BeforeEach;
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
class AutorControllerTest {

    @Mock
    private AutorUseCase autorUseCase;

    @InjectMocks
    private AutorController autorController;

    private Autor autor;

    @BeforeEach
    void setUp() {
        autor = new Autor();
        autor.setIdAutor("AUT-1");
        autor.setNombre("Gabriel García Márquez");
    }



    @Test
    void listarAutores_ConsultaUseCase_RetornaListaYEstado200() {

        List<Autor> autoresEsperados = List.of(autor);

        when(autorUseCase.listarAutores())
                .thenReturn(autoresEsperados);


        ResponseEntity<List<Autor>> respuesta =
                autorController.listarAutores();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(autoresEsperados, respuesta.getBody());
        verify(autorUseCase).listarAutores();
    }



    @Test
    void buscarAutorPorId_AutorExiste_RetornaAutorYEstado200() {

        String id = "AUT-1";

        when(autorUseCase.buscarAutorPorId(id))
                .thenReturn(autor);


        ResponseEntity<Autor> respuesta =
                autorController.buscarAutorPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(autor, respuesta.getBody());
        verify(autorUseCase).buscarAutorPorId(id);
    }

    @Test
    void buscarAutorPorId_AutorNoExiste_PropagaExcepcion() {

        String id = "AUT-INEXISTENTE";

        when(autorUseCase.buscarAutorPorId(id))
                .thenThrow(new AutorNoEncontradoException(
                        "Autor no encontrado con id: " + id
                ));


        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorController.buscarAutorPorId(id)
        );


        assertEquals(
                "Autor no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(autorUseCase).buscarAutorPorId(id);
    }



    @Test
    void guardarAutor_AutorValido_RetornaAutorYEstado201() {

        when(autorUseCase.guardarAutor(autor))
                .thenReturn(autor);


        ResponseEntity<Autor> respuesta =
                autorController.guardarAutor(autor);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(autor, respuesta.getBody());
        verify(autorUseCase).guardarAutor(autor);
    }



    @Test
    void actualizarAutor_AutorValido_RetornaAutorYEstado200() {

        String id = "AUT-1";

        when(autorUseCase.actualizarAutor(id, autor))
                .thenReturn(autor);


        ResponseEntity<Autor> respuesta =
                autorController.actualizarAutor(id, autor);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(autor, respuesta.getBody());
        verify(autorUseCase).actualizarAutor(id, autor);
    }



    @Test
    void eliminarAutor_AutorExiste_RetornaMensajeYEstado200() {

        String id = "AUT-1";


        ResponseEntity<String> respuesta =
                autorController.eliminarAutor(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Autor eliminado con exito", respuesta.getBody());
        verify(autorUseCase).eliminarAutor(id);
    }
}