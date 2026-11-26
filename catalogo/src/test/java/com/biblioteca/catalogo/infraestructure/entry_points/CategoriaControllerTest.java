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

    // LISTAR CATEGORÍAS

    @Test
    void listarCategorias_RetornaListaYEstado200() {
        // Arrange
        List<Categoria> categoriasEsperadas = List.of(categoria);

        when(categoriaUseCase.listarCategorias())
                .thenReturn(categoriasEsperadas);

        // Act
        ResponseEntity<List<Categoria>> respuesta =
                categoriaController.listarCategorias();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoriasEsperadas, respuesta.getBody());
        verify(categoriaUseCase).listarCategorias();
    }

    // BUSCAR CATEGORÍA POR ID

    @Test
    void buscarCategoriaPorId_CategoriaExiste_RetornaCategoriaYEstado200() {
        // Arrange
        String id = "CAT-1";

        when(categoriaUseCase.buscarCategoriaPorId(id))
                .thenReturn(categoria);

        // Act
        ResponseEntity<Categoria> respuesta =
                categoriaController.buscarCategoriaPorId(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).buscarCategoriaPorId(id);
    }

    @Test
    void buscarCategoriaPorId_CategoriaNoExiste_PropagaExcepcion() {
        // Arrange
        String id = "CAT-INEXISTENTE";

        when(categoriaUseCase.buscarCategoriaPorId(id))
                .thenThrow(new CategoriaNoEncontradaException(
                        "Categoria no encontrada con id: " + id
                ));

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaController.buscarCategoriaPorId(id)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(categoriaUseCase).buscarCategoriaPorId(id);
    }

    // GUARDAR CATEGORÍA

    @Test
    void guardarCategoria_CategoriaValida_RetornaCategoriaYEstado201() {
        // Arrange
        when(categoriaUseCase.guardarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        ResponseEntity<Categoria> respuesta =
                categoriaController.guardarCategoria(categoria);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).guardarCategoria(categoria);
    }

    // ACTUALIZAR CATEGORÍA

    @Test
    void actualizarCategoria_CategoriaValida_RetornaCategoriaYEstado200() {
        // Arrange
        String id = "CAT-1";

        when(categoriaUseCase.actualizarCategoria(id, categoria))
                .thenReturn(categoria);

        // Act
        ResponseEntity<Categoria> respuesta =
                categoriaController.actualizarCategoria(id, categoria);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(categoria, respuesta.getBody());
        verify(categoriaUseCase).actualizarCategoria(id, categoria);
    }

    // ELIMINAR CATEGORÍA

    @Test
    void eliminarCategoria_CategoriaExiste_RetornaMensajeYEstado200() {
        // Arrange
        String id = "CAT-1";

        // Act
        ResponseEntity<String> respuesta =
                categoriaController.eliminarCategoria(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Categoria eliminada con exito", respuesta.getBody());
        verify(categoriaUseCase).eliminarCategoria(id);
    }

    @Test
    void eliminarCategoria_CategoriaNoExiste_PropagaExcepcion() {
        // Arrange
        String id = "CAT-INEXISTENTE";

        doThrow(new CategoriaNoEncontradaException(
                "Categoria no encontrada con id: " + id
        )).when(categoriaUseCase).eliminarCategoria(id);

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaController.eliminarCategoria(id)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(categoriaUseCase).eliminarCategoria(id);
    }
}