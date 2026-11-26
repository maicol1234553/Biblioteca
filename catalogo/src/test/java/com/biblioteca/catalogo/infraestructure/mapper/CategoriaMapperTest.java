package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.infraestructure.driver_adapters.CategoriaData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaMapperTest {

    private final CategoriaMapper mapper = new CategoriaMapper();

    @Test
    void toCategoria_cuandoCategoriaDataEsNull_debeRetornarNull() {
        // Act
        Categoria resultado = mapper.toCategoria(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toCategoria_debeCopiarTodosLosCampos() {
        // Arrange
        CategoriaData categoriaData = new CategoriaData(
                "categoria-1",
                "Ciencia ficción",
                "Libros de ciencia ficción"
        );

        // Act
        Categoria resultado = mapper.toCategoria(categoriaData);

        // Assert
        assertAll(
                () -> assertEquals("categoria-1", resultado.getIdCategoria()),
                () -> assertEquals("Ciencia ficción", resultado.getNombre()),
                () -> assertEquals(
                        "Libros de ciencia ficción",
                        resultado.getDescripcion()
                )
        );
    }

    @Test
    void toCategoriaData_debeCopiarTodosLosCampos() {
        // Arrange
        Categoria categoria = new Categoria(
                "categoria-1",
                "Ciencia ficción",
                "Libros de ciencia ficción"
        );

        // Act
        CategoriaData resultado = mapper.toCategoriaData(categoria);

        // Assert
        assertAll(
                () -> assertEquals("categoria-1", resultado.getIdCategoria()),
                () -> assertEquals("Ciencia ficción", resultado.getNombre()),
                () -> assertEquals(
                        "Libros de ciencia ficción",
                        resultado.getDescripcion()
                )
        );
    }

    @Test
    void toCategoriaData_cuandoCategoriaEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toCategoriaData(null)
        );
    }
}