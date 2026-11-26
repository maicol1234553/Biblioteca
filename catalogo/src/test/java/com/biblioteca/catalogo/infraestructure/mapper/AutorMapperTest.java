package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.infraestructure.driver_adapters.AutorData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutorMapperTest {

    private final AutorMapper mapper = new AutorMapper();

    @Test
    void toAutor_cuandoAutorDataEsNull_debeRetornarNull() {
        // Act
        Autor resultado = mapper.toAutor(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toAutor_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDate fechaNacimiento = LocalDate.of(1927, 3, 6);
        AutorData autorData = new AutorData(
                "autor-1",
                "Gabriel",
                "García Márquez",
                "Colombiana",
                fechaNacimiento,
                "Escritor y periodista colombiano"
        );

        // Act
        Autor resultado = mapper.toAutor(autorData);

        // Assert
        assertAll(
                () -> assertEquals("autor-1", resultado.getIdAutor()),
                () -> assertEquals("Gabriel", resultado.getNombre()),
                () -> assertEquals("García Márquez", resultado.getApellido()),
                () -> assertEquals("Colombiana", resultado.getNacionalidad()),
                () -> assertEquals(fechaNacimiento, resultado.getFechaNacimiento()),
                () -> assertEquals(
                        "Escritor y periodista colombiano",
                        resultado.getBiografia()
                )
        );
    }

    @Test
    void toAutorData_debeCopiarTodosLosCampos() {
        // Arrange
        LocalDate fechaNacimiento = LocalDate.of(1927, 3, 6);
        Autor autor = new Autor(
                "autor-1",
                "Gabriel",
                "García Márquez",
                "Colombiana",
                fechaNacimiento,
                "Escritor y periodista colombiano"
        );

        // Act
        AutorData resultado = mapper.toAutorData(autor);

        // Assert
        assertAll(
                () -> assertEquals("autor-1", resultado.getIdAutor()),
                () -> assertEquals("Gabriel", resultado.getNombre()),
                () -> assertEquals("García Márquez", resultado.getApellido()),
                () -> assertEquals("Colombiana", resultado.getNacionalidad()),
                () -> assertEquals(fechaNacimiento, resultado.getFechaNacimiento()),
                () -> assertEquals(
                        "Escritor y periodista colombiano",
                        resultado.getBiografia()
                )
        );
    }

    @Test
    void toAutorData_cuandoAutorEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(NullPointerException.class, () -> mapper.toAutorData(null));
    }
}