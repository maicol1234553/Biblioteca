package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.infraestructure.driver_adapters.AutorData;
import com.biblioteca.catalogo.infraestructure.driver_adapters.CategoriaData;
import com.biblioteca.catalogo.infraestructure.driver_adapters.EditorialData;
import com.biblioteca.catalogo.infraestructure.driver_adapters.LibroData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroMapperTest {

    @Mock
    private AutorMapper autorMapper;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private LibroMapper mapper;

    @Test
    void toLibro_cuandoLibroDataEsNull_debeRetornarNull() {
        // Act
        Libro resultado = mapper.toLibro(null);

        // Assert
        assertNull(resultado);
    }

    @Test
    void toLibro_debeMapearCamposYRelaciones() {
        // Arrange
        LocalDate fechaPublicacion = LocalDate.of(1967, 5, 30);
        LocalDateTime fechaRegistro = LocalDateTime.of(2026, 1, 10, 12, 0);

        CategoriaData categoriaData = mock(CategoriaData.class);
        Categoria categoria = mock(Categoria.class);
        AutorData autorData = mock(AutorData.class);
        Autor autor = mock(Autor.class);
        EditorialData editorialData = mock(EditorialData.class);

        LibroData libroData = new LibroData();
        libroData.setIdLibro("libro-1");
        libroData.setIsbn("9780307474728");
        libroData.setTitulo("Cien años de soledad");
        libroData.setSubtitulo("Edición especial");
        libroData.setFechaPublicacion(fechaPublicacion);
        libroData.setIdioma("Español");
        libroData.setNumeroPaginas(471);
        libroData.setDescripcion("Novela de Gabriel García Márquez");
        libroData.setPortadaUrl("https://ejemplo.com/portada.jpg");
        libroData.setEditorial(editorialData);
        libroData.setCategorias(new LinkedHashSet<>(Set.of(categoriaData)));
        libroData.setAutores(new LinkedHashSet<>(Set.of(autorData)));
        libroData.setFechaRegistro(fechaRegistro);

        when(editorialData.getIdEditorial()).thenReturn("editorial-1");
        when(editorialData.getNombre()).thenReturn("Editorial Sudamericana");
        when(categoriaMapper.toCategoria(categoriaData)).thenReturn(categoria);
        when(autorMapper.toAutor(autorData)).thenReturn(autor);

        // Act
        Libro resultado = mapper.toLibro(libroData);

        // Assert
        assertAll(
                () -> assertEquals("libro-1", resultado.getIdLibro()),
                () -> assertEquals("9780307474728", resultado.getIsbn()),
                () -> assertEquals("Cien años de soledad", resultado.getTitulo()),
                () -> assertEquals("Edición especial", resultado.getSubtitulo()),
                () -> assertEquals(fechaPublicacion, resultado.getFechaPublicacion()),
                () -> assertEquals("Español", resultado.getIdioma()),
                () -> assertEquals(471, resultado.getNumeroPaginas()),
                () -> assertEquals(
                        "Novela de Gabriel García Márquez",
                        resultado.getDescripcion()
                ),
                () -> assertEquals(
                        "https://ejemplo.com/portada.jpg",
                        resultado.getPortadaUrl()
                ),
                () -> assertEquals("editorial-1", resultado.getIdEditorial()),
                () -> assertEquals(
                        "Editorial Sudamericana",
                        resultado.getNombreEditorial()
                ),
                () -> assertSame(categoria, resultado.getCategorias().get(0)),
                () -> assertSame(autor, resultado.getAutores().get(0)),
                () -> assertEquals(fechaRegistro, resultado.getFechaRegistro())
        );
    }

    @Test
    void toLibro_cuandoRelacionesSonNull_debeCrearListasVacias() {
        // Arrange
        LibroData libroData = new LibroData();
        libroData.setCategorias(null);
        libroData.setAutores(null);

        // Act
        Libro resultado = mapper.toLibro(libroData);

        // Assert
        assertAll(
                () -> assertEquals(List.of(), resultado.getCategorias()),
                () -> assertEquals(List.of(), resultado.getAutores())
        );
    }

    @Test
    void toLibroData_debeMapearLosCamposEscalares() {
        // Arrange
        LocalDate fechaPublicacion = LocalDate.of(1967, 5, 30);
        LocalDateTime fechaRegistro = LocalDateTime.of(2026, 1, 10, 12, 0);

        Libro libro = mock(Libro.class);
        when(libro.getIdLibro()).thenReturn("libro-1");
        when(libro.getIsbn()).thenReturn("9780307474728");
        when(libro.getTitulo()).thenReturn("Cien años de soledad");
        when(libro.getSubtitulo()).thenReturn("Edición especial");
        when(libro.getFechaPublicacion()).thenReturn(fechaPublicacion);
        when(libro.getIdioma()).thenReturn("Español");
        when(libro.getNumeroPaginas()).thenReturn(471);
        when(libro.getDescripcion()).thenReturn("Novela de Gabriel García Márquez");
        when(libro.getPortadaUrl()).thenReturn("https://ejemplo.com/portada.jpg");
        when(libro.getFechaRegistro()).thenReturn(fechaRegistro);

        // Act
        LibroData resultado = mapper.toLibroData(libro);

        // Assert
        assertAll(
                () -> assertEquals("libro-1", resultado.getIdLibro()),
                () -> assertEquals("9780307474728", resultado.getIsbn()),
                () -> assertEquals("Cien años de soledad", resultado.getTitulo()),
                () -> assertEquals("Edición especial", resultado.getSubtitulo()),
                () -> assertEquals(fechaPublicacion, resultado.getFechaPublicacion()),
                () -> assertEquals("Español", resultado.getIdioma()),
                () -> assertEquals(471, resultado.getNumeroPaginas()),
                () -> assertEquals(
                        "Novela de Gabriel García Márquez",
                        resultado.getDescripcion()
                ),
                () -> assertEquals(
                        "https://ejemplo.com/portada.jpg",
                        resultado.getPortadaUrl()
                ),
                () -> assertEquals(fechaRegistro, resultado.getFechaRegistro())
        );
    }

    @Test
    void toLibroData_cuandoLibroEsNull_debeLanzarNullPointerException() {
        // Act y Assert
        assertThrows(
                NullPointerException.class,
                () -> mapper.toLibroData(null)
        );
    }
}