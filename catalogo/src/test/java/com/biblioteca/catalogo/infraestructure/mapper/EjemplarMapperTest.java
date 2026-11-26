package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.infraestructure.driver_adapters.EjemplarData;
import com.biblioteca.catalogo.infraestructure.driver_adapters.LibroData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EjemplarMapperTest {

    private final EjemplarMapper mapper = new EjemplarMapper();

    @Test
    void toEjemplar_cuandoEjemplarDataEsNull_debeRetornarNull() {

        Ejemplar resultado = mapper.toEjemplar(null);


        assertNull(resultado);
    }

    @Test
    void toEjemplar_cuandoTieneLibro_debeCopiarLosCampos() {

        LibroData libro = mock(LibroData.class);
        when(libro.getIdLibro()).thenReturn("libro-1");
        when(libro.getTitulo()).thenReturn("Cien años de soledad");

        EjemplarData ejemplarData = new EjemplarData(
                "ejemplar-1",
                libro,
                "INV-001",
                "Estante A",
                null,
                null,
                "Buen estado"
        );


        Ejemplar resultado = mapper.toEjemplar(ejemplarData);


        assertAll(
                () -> assertEquals("ejemplar-1", resultado.getIdEjemplar()),
                () -> assertEquals("libro-1", resultado.getIdLibro()),
                () -> assertEquals("Cien años de soledad", resultado.getTituloLibro()),
                () -> assertEquals("INV-001", resultado.getCodigoInventario()),
                () -> assertEquals("Estante A", resultado.getUbicacion()),
                () -> assertNull(resultado.getEstado()),
                () -> assertNull(resultado.getFechaAdquisicion()),
                () -> assertEquals("Buen estado", resultado.getObservaciones())
        );
    }

    @Test
    void toEjemplar_cuandoLibroEsNull_debeDejarNulosSusDatos() {

        EjemplarData ejemplarData = new EjemplarData(
                "ejemplar-1",
                null,
                "INV-001",
                "Estante A",
                null,
                null,
                "Buen estado"
        );


        Ejemplar resultado = mapper.toEjemplar(ejemplarData);


        assertAll(
                () -> assertNull(resultado.getIdLibro()),
                () -> assertNull(resultado.getTituloLibro())
        );
    }

    @Test
    void toEjemplarData_debeCopiarLosCamposYAsignarElLibro() {

        Ejemplar ejemplar = new Ejemplar(
                "ejemplar-1",
                "libro-1",
                "Cien años de soledad",
                "INV-001",
                "Estante A",
                null,
                null,
                "Buen estado"
        );
        LibroData libro = mock(LibroData.class);


        EjemplarData resultado = mapper.toEjemplarData(ejemplar, libro);


        assertAll(
                () -> assertEquals("ejemplar-1", resultado.getIdEjemplar()),
                () -> assertSame(libro, resultado.getLibro()),
                () -> assertEquals("INV-001", resultado.getCodigoInventario()),
                () -> assertEquals("Estante A", resultado.getUbicacion()),
                () -> assertNull(resultado.getEstado()),
                () -> assertNull(resultado.getFechaAdquisicion()),
                () -> assertEquals("Buen estado", resultado.getObservaciones())
        );
    }

    @Test
    void toEjemplarData_cuandoEjemplarEsNull_debeLanzarNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> mapper.toEjemplarData(null, null)
        );
    }
}