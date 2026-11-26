package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.infraestructure.driver_adapters.EditorialData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditorialMapperTest {

    private final EditorialMapper mapper = new EditorialMapper();

    @Test
    void toEditorial_cuandoEditorialDataEsNull_debeRetornarNull() {

        Editorial resultado = mapper.toEditorial(null);


        assertNull(resultado);
    }

    @Test
    void toEditorial_debeCopiarTodosLosCampos() {

        EditorialData editorialData = new EditorialData(
                "editorial-1",
                "Planeta",
                "España",
                "https://www.planetadelibros.com",
                "contacto@planeta.com"
        );


        Editorial resultado = mapper.toEditorial(editorialData);


        assertAll(
                () -> assertEquals("editorial-1", resultado.getIdEditorial()),
                () -> assertEquals("Planeta", resultado.getNombre()),
                () -> assertEquals("España", resultado.getPais()),
                () -> assertEquals(
                        "https://www.planetadelibros.com",
                        resultado.getSitioWeb()
                ),
                () -> assertEquals(
                        "contacto@planeta.com",
                        resultado.getCorreoContacto()
                )
        );
    }

    @Test
    void toEditorialData_debeCopiarTodosLosCampos() {

        Editorial editorial = new Editorial(
                "editorial-1",
                "Planeta",
                "España",
                "https://www.planetadelibros.com",
                "contacto@planeta.com"
        );


        EditorialData resultado = mapper.toEditorialData(editorial);


        assertAll(
                () -> assertEquals("editorial-1", resultado.getIdEditorial()),
                () -> assertEquals("Planeta", resultado.getNombre()),
                () -> assertEquals("España", resultado.getPais()),
                () -> assertEquals(
                        "https://www.planetadelibros.com",
                        resultado.getSitioWeb()
                ),
                () -> assertEquals(
                        "contacto@planeta.com",
                        resultado.getCorreoContacto()
                )
        );
    }

    @Test
    void toEditorialData_cuandoEditorialEsNull_debeLanzarNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> mapper.toEditorialData(null)
        );
    }
}