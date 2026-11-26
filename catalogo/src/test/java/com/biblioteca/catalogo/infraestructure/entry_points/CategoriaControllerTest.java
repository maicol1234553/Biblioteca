package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.usecase.CategoriaUseCase;
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
class CategoriaControllerTest {

    @Mock
    private CategoriaUseCase categoriaUseCase;

    @InjectMocks
    private CategoriaController categoriaController;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setIdCategoria("CAT-1");
        categoria.setNombre("Ficción");
        categoria.setDescripcion("Libros de ficción");
    }



    @Test
    void listarCategorias_RetornaListaYEstado200() {

        List<Categoria> categoriasEsperadas = List.of(categoria);

        when(categoriaUseCase.listarCategorias())
                .thenReturn(categoriasEsperadas);


        ResponseEntity<List<Categoria>> respuesta =
                categoriaController.listarCategorias();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoriasEsperadas, respuesta.getBody());
        verify(categoriaUseCase).listarCategorias();
    }



    @Test
    void buscarCategoriaPorId_CategoriaExiste_RetornaCategoriaYEstado200() {

        String id = "CAT-1";

        when(categoriaUseCase.buscarCategoriaPorId(id))
                .thenReturn(categoria);


        ResponseEntity<Categoria> respuesta =
                categoriaController.buscarCategoriaPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).buscarCategoriaPorId(id);
    }

    @Test
    void buscarCategoriaPorId_CategoriaNoExiste_PropagaExcepcion() {

        String id = "CAT-INEXISTENTE";

        when(categoriaUseCase.buscarCategoriaPorId(id))
                .thenThrow(new CategoriaNoEncontradaException(
                        "Categoria no encontrada con id: " + id
                ));


        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaController.buscarCategoriaPorId(id)
        );


        assertEquals(
                "Categoria no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(categoriaUseCase).buscarCategoriaPorId(id);
    }



    @Test
    void guardarCategoria_CategoriaValida_RetornaCategoriaYEstado201() {

        when(categoriaUseCase.guardarCategoria(categoria))
                .thenReturn(categoria);


        ResponseEntity<Categoria> respuesta =
                categoriaController.guardarCategoria(categoria);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).guardarCategoria(categoria);
    }



    @Test
    void actualizarCategoria_CategoriaValida_RetornaCategoriaYEstado200() {

        String id = "CAT-1";

        when(categoriaUseCase.actualizarCategoria(id, categoria))
                .thenReturn(categoria);


        ResponseEntity<Categoria> respuesta =
                categoriaController.actualizarCategoria(id, categoria);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).actualizarCategoria(id, categoria);
    }



    @Test
    void eliminarCategoria_CategoriaExiste_RetornaMensajeYEstado200() {

        String id = "CAT-1";


        ResponseEntity<String> respuesta =
                categoriaController.eliminarCategoria(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Categoria eliminada con exito", respuesta.getBody());
        verify(categoriaUseCase).eliminarCategoria(id);
    }

    @Test
    void eliminarCategoria_CategoriaNoExiste_PropagaExcepcion() {

        String id = "CAT-INEXISTENTE";

        doThrow(new CategoriaNoEncontradaException(
                "Categoria no encontrada con id: " + id
        )).when(categoriaUseCase).eliminarCategoria(id);


        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaController.eliminarCategoria(id)
        );


        assertEquals(
                "Categoria no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(categoriaUseCase).eliminarCategoria(id);
    }
}