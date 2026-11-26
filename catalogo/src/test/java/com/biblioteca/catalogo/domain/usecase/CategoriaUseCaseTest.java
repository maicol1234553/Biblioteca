package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaUseCaseTest {

    @Mock
    private CategoriaGateway categoriaGateway;

    @InjectMocks
    private CategoriaUseCase categoriaUseCase;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = crearCategoriaValida();
    }

    // GUARDAR CATEGORÍA

    @Test
    void guardarCategoria_DatosValidosSinId_GeneraIdYGuarda() {
        // Arrange
        categoria.setIdCategoria(null);

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(null);

        when(categoriaGateway.guardarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        Categoria resultado = categoriaUseCase.guardarCategoria(categoria);

        // Assert
        assertSame(categoria, resultado);
        assertNotNull(resultado.getIdCategoria());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdCategoria()));

        verify(categoriaGateway).buscarCategoriaPorNombre(categoria.getNombre());
        verify(categoriaGateway).guardarCategoria(categoria);
    }

    @Test
    void guardarCategoria_IdEnBlanco_GeneraIdYGuarda() {
        // Arrange
        categoria.setIdCategoria(" ");

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(null);

        when(categoriaGateway.guardarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        Categoria resultado = categoriaUseCase.guardarCategoria(categoria);

        // Assert
        assertNotNull(resultado.getIdCategoria());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdCategoria()));
        verify(categoriaGateway).guardarCategoria(categoria);
    }

    @Test
    void guardarCategoria_IdExistente_LoConserva() {
        // Arrange
        categoria.setIdCategoria("CAT-1");

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(null);

        when(categoriaGateway.guardarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        Categoria resultado = categoriaUseCase.guardarCategoria(categoria);

        // Assert
        assertEquals("CAT-1", resultado.getIdCategoria());
        verify(categoriaGateway).guardarCategoria(categoria);
    }

    @Test
    void guardarCategoria_NombreDuplicado_LanzaExcepcion() {
        // Arrange
        Categoria existente = crearCategoriaValida();
        existente.setIdCategoria("CAT-2");

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(existente);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> categoriaUseCase.guardarCategoria(categoria)
        );

        // Assert
        assertEquals(
                "Ya existe una categoria con el nombre: " + categoria.getNombre(),
                excepcion.getMessage()
        );
        verify(categoriaGateway, never())
                .guardarCategoria(any(Categoria.class));
    }

    @Test
    void guardarCategoria_CategoriaNula_LanzaExcepcion() {
        // Arrange
        Categoria categoriaNula = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(categoriaNula)
        );

        // Assert
        assertEquals("La categoria es obligatoria", excepcion.getMessage());
        verifyNoInteractions(categoriaGateway);
    }

    @Test
    void guardarCategoria_NombreNulo_LanzaExcepcion() {
        // Arrange
        categoria.setNombre(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(categoria)
        );

        // Assert
        assertEquals(
                "El nombre de la categoria es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(categoriaGateway);
    }

    @Test
    void guardarCategoria_NombreEnBlanco_LanzaExcepcion() {
        // Arrange
        categoria.setNombre(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaUseCase.guardarCategoria(categoria)
        );

        // Assert
        assertEquals(
                "El nombre de la categoria es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(categoriaGateway);
    }

    // ACTUALIZAR CATEGORÍA

    @Test
    void actualizarCategoria_DatosValidos_AsignaIdYActualiza() {
        // Arrange
        String idCategoria = "CAT-1";
        Categoria existente = crearCategoriaValida();
        existente.setIdCategoria(idCategoria);

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(existente);

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(null);

        when(categoriaGateway.actualizarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        Categoria resultado =
                categoriaUseCase.actualizarCategoria(idCategoria, categoria);

        // Assert
        assertSame(categoria, resultado);
        assertEquals(idCategoria, resultado.getIdCategoria());

        verify(categoriaGateway).buscarCategoriaPorId(idCategoria);
        verify(categoriaGateway).buscarCategoriaPorNombre(categoria.getNombre());
        verify(categoriaGateway).actualizarCategoria(categoria);
    }

    @Test
    void actualizarCategoria_CategoriaNoExiste_LanzaExcepcion() {
        // Arrange
        String idCategoria = "ID-INEXISTENTE";

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(null);

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaUseCase.actualizarCategoria(idCategoria, categoria)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: " + idCategoria,
                excepcion.getMessage()
        );
        verify(categoriaGateway, never())
                .actualizarCategoria(any(Categoria.class));
        verify(categoriaGateway, never())
                .buscarCategoriaPorNombre(anyString());
    }

    @Test
    void actualizarCategoria_NombreDeOtraCategoria_LanzaExcepcion() {
        // Arrange
        String idCategoria = "CAT-1";

        Categoria categoriaExistente = crearCategoriaValida();
        categoriaExistente.setIdCategoria(idCategoria);

        Categoria otraCategoria = crearCategoriaValida();
        otraCategoria.setIdCategoria("CAT-2");

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(categoriaExistente);

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(otraCategoria);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> categoriaUseCase.actualizarCategoria(idCategoria, categoria)
        );

        // Assert
        assertEquals(
                "Ya existe una categoria con el nombre: " + categoria.getNombre(),
                excepcion.getMessage()
        );
        verify(categoriaGateway, never())
                .actualizarCategoria(any(Categoria.class));
    }

    @Test
    void actualizarCategoria_ConservaMismoNombre_PermiteActualizar() {
        // Arrange
        String idCategoria = "CAT-1";

        Categoria existente = crearCategoriaValida();
        existente.setIdCategoria(idCategoria);

        categoria.setNombre(existente.getNombre());

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(existente);

        when(categoriaGateway.buscarCategoriaPorNombre(categoria.getNombre()))
                .thenReturn(existente);

        when(categoriaGateway.actualizarCategoria(categoria))
                .thenReturn(categoria);

        // Act
        Categoria resultado =
                categoriaUseCase.actualizarCategoria(idCategoria, categoria);

        // Assert
        assertSame(categoria, resultado);
        assertEquals(idCategoria, resultado.getIdCategoria());
        verify(categoriaGateway).actualizarCategoria(categoria);
    }

    // BUSCAR CATEGORÍA POR ID

    @Test
    void buscarCategoriaPorId_CategoriaExiste_RetornaCategoria() {
        // Arrange
        when(categoriaGateway.buscarCategoriaPorId(categoria.getIdCategoria()))
                .thenReturn(categoria);

        // Act
        Categoria resultado =
                categoriaUseCase.buscarCategoriaPorId(categoria.getIdCategoria());

        // Assert
        assertSame(categoria, resultado);
        verify(categoriaGateway)
                .buscarCategoriaPorId(categoria.getIdCategoria());
    }

    @Test
    void buscarCategoriaPorId_CategoriaNoExiste_LanzaExcepcion() {
        // Arrange
        String idCategoria = "ID-INEXISTENTE";

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(null);

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaUseCase.buscarCategoriaPorId(idCategoria)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: " + idCategoria,
                excepcion.getMessage()
        );
    }

    // LISTAR CATEGORÍAS

    @Test
    void listarCategorias_ConsultaGateway_RetornaLista() {
        // Arrange
        List<Categoria> categoriasEsperadas = List.of(categoria);

        when(categoriaGateway.listarCategorias())
                .thenReturn(categoriasEsperadas);

        // Act
        List<Categoria> resultado = categoriaUseCase.listarCategorias();

        // Assert
        assertSame(categoriasEsperadas, resultado);
        assertEquals(1, resultado.size());
        assertEquals(categoria, resultado.get(0));
        verify(categoriaGateway).listarCategorias();
    }

    @Test
    void listarCategorias_NoHayCategorias_RetornaListaVacia() {
        // Arrange
        when(categoriaGateway.listarCategorias())
                .thenReturn(List.of());

        // Act
        List<Categoria> resultado = categoriaUseCase.listarCategorias();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ELIMINAR CATEGORÍA

    @Test
    void eliminarCategoria_CategoriaExiste_EliminaCategoria() {
        // Arrange
        String idCategoria = categoria.getIdCategoria();

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(categoria);

        // Act
        categoriaUseCase.eliminarCategoria(idCategoria);

        // Assert
        verify(categoriaGateway).buscarCategoriaPorId(idCategoria);
        verify(categoriaGateway).eliminarCategoria(idCategoria);
    }

    @Test
    void eliminarCategoria_CategoriaNoExiste_LanzaExcepcionYSinEliminar() {
        // Arrange
        String idCategoria = "ID-INEXISTENTE";

        when(categoriaGateway.buscarCategoriaPorId(idCategoria))
                .thenReturn(null);

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> categoriaUseCase.eliminarCategoria(idCategoria)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: " + idCategoria,
                excepcion.getMessage()
        );
        verify(categoriaGateway, never()).eliminarCategoria(anyString());
    }

    private Categoria crearCategoriaValida() {
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setIdCategoria("CAT-1");
        nuevaCategoria.setNombre("Ficción");
        nuevaCategoria.setDescripcion("Libros de ficción");
        return nuevaCategoria;
    }
}