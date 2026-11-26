package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroUseCaseTest {

    @Mock
    private LibroGateway libroGateway;

    @InjectMocks
    private LibroUseCase libroUseCase;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = crearLibroValido();
    }

    // GUARDAR LIBRO

    @Test
    void guardarLibro_DatosValidosSinId_GeneraIdYGuarda() {
        // Arrange
        libro.setIdLibro(null);

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(null);

        when(libroGateway.guardarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.guardarLibro(libro);

        // Assert
        assertSame(libro, resultado);
        assertNotNull(resultado.getIdLibro());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdLibro()));

        verify(libroGateway).buscarLibroPorIsbn(libro.getIsbn());
        verify(libroGateway).guardarLibro(libro);
    }

    @Test
    void guardarLibro_FechaRegistroNula_AsignarFechaActual() {
        // Arrange
        libro.setFechaRegistro(null);
        LocalDateTime antesDeGuardar = LocalDateTime.now();

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(null);

        when(libroGateway.guardarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.guardarLibro(libro);

        // Assert
        LocalDateTime despuesDeGuardar = LocalDateTime.now();

        assertNotNull(resultado.getFechaRegistro());
        assertFalse(resultado.getFechaRegistro().isBefore(antesDeGuardar));
        assertFalse(resultado.getFechaRegistro().isAfter(despuesDeGuardar));

        verify(libroGateway).guardarLibro(libro);
    }

    @Test
    void guardarLibro_IsbnNulo_PermiteGuardarSinBuscarDuplicado() {
        // Arrange
        libro.setIsbn(null);

        when(libroGateway.guardarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.guardarLibro(libro);

        // Assert
        assertSame(libro, resultado);
        verify(libroGateway, never()).buscarLibroPorIsbn(anyString());
        verify(libroGateway).guardarLibro(libro);
    }

    @Test
    void guardarLibro_IsbnDuplicado_LanzaExcepcion() {
        // Arrange
        Libro existente = crearLibroValido();
        existente.setIdLibro("LIBRO-2");

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(existente);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Ya existe un libro con el ISBN: " + libro.getIsbn(),
                excepcion.getMessage()
        );
        verify(libroGateway, never()).guardarLibro(any(Libro.class));
    }

    @Test
    void guardarLibro_LibroNulo_LanzaExcepcion() {
        // Arrange
        Libro libroNulo = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libroNulo)
        );

        // Assert
        assertEquals("El libro es obligatorio", excepcion.getMessage());
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_TituloNulo_LanzaExcepcion() {
        // Arrange
        libro.setTitulo(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "El titulo del libro es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_TituloEnBlanco_LanzaExcepcion() {
        // Arrange
        libro.setTitulo(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "El titulo del libro es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_IdEditorialNulo_LanzaExcepcion() {
        // Arrange
        libro.setIdEditorial(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "La editorial del libro es obligatoria",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_IdEditorialEnBlanco_LanzaExcepcion() {
        // Arrange
        libro.setIdEditorial(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "La editorial del libro es obligatoria",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_CategoriasNulas_LanzaExcepcion() {
        // Arrange
        libro.setCategorias(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Debe clasificar el libro en al menos una categoria",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_SinCategorias_LanzaExcepcion() {
        // Arrange
        libro.setCategorias(List.of());

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Debe clasificar el libro en al menos una categoria",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_NumeroPaginasCero_LanzaExcepcion() {
        // Arrange
        libro.setNumeroPaginas(0);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "El numero de paginas debe ser mayor a cero",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_NumeroPaginasNegativo_LanzaExcepcion() {
        // Arrange
        libro.setNumeroPaginas(-1);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "El numero de paginas debe ser mayor a cero",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_IsbnConFormatoInvalido_LanzaExcepcion() {
        // Arrange
        libro.setIsbn("ISBN-incorrecto");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> libroUseCase.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "El ISBN no tiene un formato valido",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroGateway);
    }

    @Test
    void guardarLibro_AutoresNulos_LosInicializaComoListaVacia() {
        // Arrange
        libro.setAutores(null);

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(null);

        when(libroGateway.guardarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.guardarLibro(libro);

        // Assert
        assertNotNull(resultado.getAutores());
        assertTrue(resultado.getAutores().isEmpty());
        verify(libroGateway).guardarLibro(libro);
    }

    // ACTUALIZAR LIBRO

    @Test
    void actualizarLibro_DatosValidos_AsignaIdYActualiza() {
        // Arrange
        String idLibro = "LIBRO-1";

        Libro existente = crearLibroValido();
        existente.setIdLibro(idLibro);

        libro.setIdLibro(null);

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(null);

        when(libroGateway.actualizarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.actualizarLibro(idLibro, libro);

        // Assert
        assertSame(libro, resultado);
        assertEquals(idLibro, resultado.getIdLibro());
        assertNull(resultado.getNombreEditorial());

        verify(libroGateway).buscarLibroPorId(idLibro);
        verify(libroGateway).actualizarLibro(libro);
    }

    @Test
    void actualizarLibro_CamposNulos_ConservaValoresExistentes() {
        // Arrange
        String idLibro = "LIBRO-1";

        Libro existente = crearLibroValido();
        existente.setIdLibro(idLibro);
        existente.setFechaRegistro(LocalDateTime.of(2024, 1, 10, 12, 0));

        Libro actualizacion = new Libro();
        actualizacion.setTitulo("Título actualizado");
        actualizacion.setIsbn(" ");
        actualizacion.setIdEditorial(" ");
        actualizacion.setCategorias(List.of());
        actualizacion.setAutores(List.of());

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorIsbn(existente.getIsbn()))
                .thenReturn(existente);

        when(libroGateway.actualizarLibro(actualizacion))
                .thenReturn(actualizacion);

        // Act
        Libro resultado =
                libroUseCase.actualizarLibro(idLibro, actualizacion);

        // Assert
        assertEquals(idLibro, resultado.getIdLibro());
        assertEquals(existente.getSubtitulo(), resultado.getSubtitulo());
        assertEquals(existente.getIdioma(), resultado.getIdioma());
        assertEquals(existente.getNumeroPaginas(), resultado.getNumeroPaginas());
        assertEquals(existente.getDescripcion(), resultado.getDescripcion());
        assertEquals(existente.getPortadaUrl(), resultado.getPortadaUrl());
        assertEquals(existente.getIsbn(), resultado.getIsbn());
        assertEquals(existente.getIdEditorial(), resultado.getIdEditorial());
        assertSame(existente.getCategorias(), resultado.getCategorias());
        assertSame(existente.getAutores(), resultado.getAutores());
        assertEquals(existente.getFechaRegistro(), resultado.getFechaRegistro());
        assertNull(resultado.getNombreEditorial());

        verify(libroGateway).actualizarLibro(actualizacion);
    }

    @Test
    void actualizarLibro_LibroNoExiste_LanzaExcepcion() {
        // Arrange
        String idLibro = "ID-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> libroUseCase.actualizarLibro(idLibro, libro)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(libroGateway, never())
                .actualizarLibro(any(Libro.class));
    }

    @Test
    void actualizarLibro_IsbnDeOtroLibro_LanzaExcepcion() {
        // Arrange
        String idLibro = "LIBRO-1";

        Libro existente = crearLibroValido();
        existente.setIdLibro(idLibro);

        Libro otroLibro = crearLibroValido();
        otroLibro.setIdLibro("LIBRO-2");

        libro.setIsbn("978-0-306-40615-7");

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorIsbn(libro.getIsbn()))
                .thenReturn(otroLibro);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> libroUseCase.actualizarLibro(idLibro, libro)
        );

        // Assert
        assertEquals(
                "Ya existe un libro con el ISBN: " + libro.getIsbn(),
                excepcion.getMessage()
        );
        verify(libroGateway, never())
                .actualizarLibro(any(Libro.class));
    }

    @Test
    void actualizarLibro_MismoIsbnDelLibro_LoPermite() {
        // Arrange
        String idLibro = "LIBRO-1";

        Libro existente = crearLibroValido();
        existente.setIdLibro(idLibro);
        libro.setIsbn(existente.getIsbn());

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorIsbn(existente.getIsbn()))
                .thenReturn(existente);

        when(libroGateway.actualizarLibro(libro))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.actualizarLibro(idLibro, libro);

        // Assert
        assertSame(libro, resultado);
        assertEquals(idLibro, resultado.getIdLibro());
        verify(libroGateway).actualizarLibro(libro);
    }

    // BUSCAR LIBRO POR ID

    @Test
    void buscarLibroPorId_LibroExiste_RetornaLibro() {
        // Arrange
        when(libroGateway.buscarLibroPorId(libro.getIdLibro()))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.buscarLibroPorId(libro.getIdLibro());

        // Assert
        assertSame(libro, resultado);
        verify(libroGateway).buscarLibroPorId(libro.getIdLibro());
    }

    @Test
    void buscarLibroPorId_LibroNoExiste_LanzaExcepcion() {
        // Arrange
        String idLibro = "ID-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> libroUseCase.buscarLibroPorId(idLibro)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
    }

    // BUSCAR LIBRO POR ISBN

    @Test
    void buscarLibroPorIsbn_LibroExiste_RetornaLibro() {
        // Arrange
        String isbn = libro.getIsbn();

        when(libroGateway.buscarLibroPorIsbn(isbn))
                .thenReturn(libro);

        // Act
        Libro resultado = libroUseCase.buscarLibroPorIsbn(isbn);

        // Assert
        assertSame(libro, resultado);
        verify(libroGateway).buscarLibroPorIsbn(isbn);
    }

    @Test
    void buscarLibroPorIsbn_LibroNoExiste_RetornaNull() {
        // Arrange
        String isbn = "9780306406157";

        when(libroGateway.buscarLibroPorIsbn(isbn))
                .thenReturn(null);

        // Act
        Libro resultado = libroUseCase.buscarLibroPorIsbn(isbn);

        // Assert
        assertNull(resultado);
        verify(libroGateway).buscarLibroPorIsbn(isbn);
    }

    // LISTAR LIBROS

    @Test
    void listarLibros_ConsultaGateway_RetornaLista() {
        // Arrange
        List<Libro> librosEsperados = List.of(libro);

        when(libroGateway.listarLibros())
                .thenReturn(librosEsperados);

        // Act
        List<Libro> resultado = libroUseCase.listarLibros();

        // Assert
        assertSame(librosEsperados, resultado);
        assertEquals(1, resultado.size());
        assertSame(libro, resultado.get(0));
        verify(libroGateway).listarLibros();
    }

    @Test
    void listarLibros_SinResultados_RetornaListaVacia() {
        // Arrange
        when(libroGateway.listarLibros())
                .thenReturn(List.of());

        // Act
        List<Libro> resultado = libroUseCase.listarLibros();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // BUSCAR LIBROS CON FILTROS

    @Test
    void buscarLibros_BusquedaNula_ListaTodosLosLibros() {
        // Arrange
        List<Libro> librosEsperados = List.of(libro);

        when(libroGateway.listarLibros())
                .thenReturn(librosEsperados);

        // Act
        List<Libro> resultado = libroUseCase.buscarLibros(null);

        // Assert
        assertSame(librosEsperados, resultado);
        verify(libroGateway).listarLibros();
        verify(libroGateway, never()).buscarLibros(any(BusquedaLibro.class));
    }

    @Test
    void buscarLibros_FiltrosConEspacios_LimpiaValoresAntesDeBuscar() {
        // Arrange
        BusquedaLibro busqueda = new BusquedaLibro(
                "  García Márquez  ",
                "  AUTOR-1 ",
                " CAT-1 ",
                " EDIT-1 ",
                " es "
        );

        ArgumentCaptor<BusquedaLibro> busquedaCaptor =
                ArgumentCaptor.forClass(BusquedaLibro.class);

        when(libroGateway.buscarLibros(any(BusquedaLibro.class)))
                .thenReturn(List.of(libro));

        // Act
        List<Libro> resultado = libroUseCase.buscarLibros(busqueda);

        // Assert
        assertEquals(1, resultado.size());

        verify(libroGateway).buscarLibros(busquedaCaptor.capture());

        BusquedaLibro busquedaEnviada = busquedaCaptor.getValue();

        assertEquals("García Márquez", busquedaEnviada.texto());
        assertEquals("AUTOR-1", busquedaEnviada.idAutor());
        assertEquals("CAT-1", busquedaEnviada.idCategoria());
        assertEquals("EDIT-1", busquedaEnviada.idEditorial());
        assertEquals("es", busquedaEnviada.idioma());
    }

    @Test
    void buscarLibros_FiltrosNulosYEnBlanco_LosConvierteEnNull() {
        // Arrange
        BusquedaLibro busqueda = new BusquedaLibro(
                null,
                " ",
                "\t",
                null,
                " "
        );

        ArgumentCaptor<BusquedaLibro> busquedaCaptor =
                ArgumentCaptor.forClass(BusquedaLibro.class);

        when(libroGateway.buscarLibros(any(BusquedaLibro.class)))
                .thenReturn(List.of());

        // Act
        List<Libro> resultado = libroUseCase.buscarLibros(busqueda);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(libroGateway).buscarLibros(busquedaCaptor.capture());

        BusquedaLibro busquedaEnviada = busquedaCaptor.getValue();

        assertNull(busquedaEnviada.texto());
        assertNull(busquedaEnviada.idAutor());
        assertNull(busquedaEnviada.idCategoria());
        assertNull(busquedaEnviada.idEditorial());
        assertNull(busquedaEnviada.idioma());
    }

    // ELIMINAR LIBRO

    @Test
    void eliminarLibro_LibroExiste_EliminaLibro() {
        // Arrange
        String idLibro = libro.getIdLibro();

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(libro);

        // Act
        libroUseCase.eliminarLibro(idLibro);

        // Assert
        verify(libroGateway).buscarLibroPorId(idLibro);
        verify(libroGateway).eliminarLibro(idLibro);
    }

    @Test
    void eliminarLibro_LibroNoExiste_LanzaExcepcionYSinEliminar() {
        // Arrange
        String idLibro = "ID-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> libroUseCase.eliminarLibro(idLibro)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(libroGateway, never()).eliminarLibro(anyString());
    }

    private Libro crearLibroValido() {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria("CAT-1");
        categoria.setNombre("Ficción");

        Autor autor = new Autor();
        autor.setIdAutor("AUTOR-1");
        autor.setNombre("Gabriel García Márquez");

        Libro nuevoLibro = new Libro();
        nuevoLibro.setIdLibro("LIBRO-1");
        nuevoLibro.setIsbn("9780306406157");
        nuevoLibro.setTitulo("Cien años de soledad");
        nuevoLibro.setSubtitulo("Edición de prueba");
        nuevoLibro.setIdioma("es");
        nuevoLibro.setNumeroPaginas(417);
        nuevoLibro.setDescripcion("Descripción de prueba");
        nuevoLibro.setPortadaUrl("https://ejemplo.com/portada.jpg");
        nuevoLibro.setIdEditorial("EDIT-1");
        nuevoLibro.setNombreEditorial("Editorial de prueba");
        nuevoLibro.setCategorias(List.of(categoria));
        nuevoLibro.setAutores(List.of(autor));
        nuevoLibro.setFechaRegistro(LocalDateTime.of(2024, 1, 10, 12, 0));

        return nuevoLibro;
    }
}