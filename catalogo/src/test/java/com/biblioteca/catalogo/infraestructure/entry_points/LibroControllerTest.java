package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.DisponibilidadLibro;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.usecase.EjemplarUseCase;
import com.biblioteca.catalogo.domain.usecase.LibroUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroControllerTest {

    @Mock
    private LibroUseCase libroUseCase;

    @Mock
    private EjemplarUseCase ejemplarUseCase;

    @InjectMocks
    private LibroController libroController;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = crearLibro();
    }



    @Test
    void listarLibros_RetornaListaYEstado200() {

        List<Libro> librosEsperados = List.of(libro);

        when(libroUseCase.listarLibros())
                .thenReturn(librosEsperados);


        ResponseEntity<List<Libro>> respuesta =
                libroController.listarLibros();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(librosEsperados, respuesta.getBody());
        verify(libroUseCase).listarLibros();
    }



    @Test
    void buscarLibros_FiltrosRecibidos_CreaBusquedaYRetornaResultados() {

        List<Libro> librosEsperados = List.of(libro);

        when(libroUseCase.buscarLibros(any(BusquedaLibro.class)))
                .thenReturn(librosEsperados);

        ArgumentCaptor<BusquedaLibro> busquedaCaptor =
                ArgumentCaptor.forClass(BusquedaLibro.class);


        ResponseEntity<List<Libro>> respuesta =
                libroController.buscarLibros(
                        "García Márquez",
                        "AUT-1",
                        "CAT-1",
                        "EDIT-1",
                        "es"
                );


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(librosEsperados, respuesta.getBody());

        verify(libroUseCase).buscarLibros(busquedaCaptor.capture());

        BusquedaLibro busquedaEnviada = busquedaCaptor.getValue();

        assertEquals("García Márquez", busquedaEnviada.texto());
        assertEquals("AUT-1", busquedaEnviada.idAutor());
        assertEquals("CAT-1", busquedaEnviada.idCategoria());
        assertEquals("EDIT-1", busquedaEnviada.idEditorial());
        assertEquals("es", busquedaEnviada.idioma());
    }

    @Test
    void buscarLibros_FiltrosNulos_PasaBusquedaConValoresNulos() {

        when(libroUseCase.buscarLibros(any(BusquedaLibro.class)))
                .thenReturn(List.of());

        ArgumentCaptor<BusquedaLibro> busquedaCaptor =
                ArgumentCaptor.forClass(BusquedaLibro.class);


        ResponseEntity<List<Libro>> respuesta =
                libroController.buscarLibros(null, null, null, null, null);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isEmpty());

        verify(libroUseCase).buscarLibros(busquedaCaptor.capture());

        BusquedaLibro busquedaEnviada = busquedaCaptor.getValue();

        assertNull(busquedaEnviada.texto());
        assertNull(busquedaEnviada.idAutor());
        assertNull(busquedaEnviada.idCategoria());
        assertNull(busquedaEnviada.idEditorial());
        assertNull(busquedaEnviada.idioma());
    }



    @Test
    void buscarLibroPorId_LibroExiste_RetornaLibroYEstado200() {

        String id = "LIB-1";

        when(libroUseCase.buscarLibroPorId(id))
                .thenReturn(libro);


        ResponseEntity<Libro> respuesta =
                libroController.buscarLibroPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(libro, respuesta.getBody());
        verify(libroUseCase).buscarLibroPorId(id);
    }

    @Test
    void buscarLibroPorId_LibroNoExiste_PropagaExcepcion() {

        String id = "LIB-INEXISTENTE";

        when(libroUseCase.buscarLibroPorId(id))
                .thenThrow(new LibroNoEncontradoException(
                        "Libro no encontrado con id: " + id
                ));


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> libroController.buscarLibroPorId(id)
        );


        assertEquals(
                "Libro no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(libroUseCase).buscarLibroPorId(id);
    }



    @Test
    void consultarDisponibilidad_LibroExiste_RetornaDisponibilidadYEstado200() {

        String id = "LIB-1";
        DisponibilidadLibro disponibilidad =
                mock(DisponibilidadLibro.class);

        when(ejemplarUseCase.consultarDisponibilidad(id))
                .thenReturn(disponibilidad);


        ResponseEntity<DisponibilidadLibro> respuesta =
                libroController.consultarDisponibilidad(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(disponibilidad, respuesta.getBody());
        verify(ejemplarUseCase).consultarDisponibilidad(id);
    }



    @Test
    void guardarLibro_RequestValido_ConvierteLibroYRetornaEstado201() {

        LibroController.LibroRequest request = crearRequestValido();

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenReturn(libro);

        ArgumentCaptor<Libro> libroCaptor =
                ArgumentCaptor.forClass(Libro.class);


        ResponseEntity<Libro> respuesta =
                libroController.guardarLibro(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(libro, respuesta.getBody());

        verify(libroUseCase).guardarLibro(libroCaptor.capture());

        Libro libroEnviado = libroCaptor.getValue();

        assertEquals(request.isbn(), libroEnviado.getIsbn());
        assertEquals(request.titulo(), libroEnviado.getTitulo());
        assertEquals(request.subtitulo(), libroEnviado.getSubtitulo());
        assertEquals(
                request.fechaPublicacion(),
                libroEnviado.getFechaPublicacion()
        );
        assertEquals(request.idioma(), libroEnviado.getIdioma());
        assertEquals(request.numeroPaginas(), libroEnviado.getNumeroPaginas());
        assertEquals(request.descripcion(), libroEnviado.getDescripcion());
        assertEquals(request.portadaUrl(), libroEnviado.getPortadaUrl());
        assertEquals(request.idEditorial(), libroEnviado.getIdEditorial());

        assertEquals(
                List.of("CAT-1", "CAT-2"),
                libroEnviado.getCategorias().stream()
                        .map(Categoria::getIdCategoria)
                        .toList()
        );

        assertEquals(
                List.of("AUT-1", "AUT-2"),
                libroEnviado.getAutores().stream()
                        .map(Autor::getIdAutor)
                        .toList()
        );
    }

    @Test
    void guardarLibro_IdsDeAutoresYCategoriasNulos_LosDejaNulos() {

        LibroController.LibroRequest request =
                new LibroController.LibroRequest(
                        "9780306406157",
                        "Cien años de soledad",
                        null,
                        LocalDate.of(1967, 5, 30),
                        "es",
                        417,
                        "Descripción de prueba",
                        null,
                        "EDIT-1",
                        null,
                        null
                );

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenReturn(libro);

        ArgumentCaptor<Libro> libroCaptor =
                ArgumentCaptor.forClass(Libro.class);


        ResponseEntity<Libro> respuesta =
                libroController.guardarLibro(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());

        verify(libroUseCase).guardarLibro(libroCaptor.capture());

        Libro libroEnviado = libroCaptor.getValue();

        assertNotNull(libroEnviado.getCategorias());
        assertTrue(libroEnviado.getCategorias().isEmpty());

        assertNotNull(libroEnviado.getAutores());
        assertTrue(libroEnviado.getAutores().isEmpty());
    }

    @Test
    void guardarLibro_FiltraIdsNulosYEnBlanco() {

        LibroController.LibroRequest request =
                new LibroController.LibroRequest(
                        "9780306406157",
                        "Cien años de soledad",
                        null,
                        null,
                        "es",
                        417,
                        null,
                        null,
                        "EDIT-1",
                        Arrays.asList("CAT-1", null, " ", "CAT-2"),
                        Arrays.asList("AUT-1", null, "\t", "AUT-2")
                );

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenReturn(libro);

        ArgumentCaptor<Libro> libroCaptor =
                ArgumentCaptor.forClass(Libro.class);


        ResponseEntity<Libro> respuesta =
                libroController.guardarLibro(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());

        verify(libroUseCase).guardarLibro(libroCaptor.capture());

        Libro libroEnviado = libroCaptor.getValue();

        assertEquals(
                List.of("CAT-1", "CAT-2"),
                libroEnviado.getCategorias().stream()
                        .map(Categoria::getIdCategoria)
                        .toList()
        );

        assertEquals(
                List.of("AUT-1", "AUT-2"),
                libroEnviado.getAutores().stream()
                        .map(Autor::getIdAutor)
                        .toList()
        );
    }



    @Test
    void actualizarLibro_RequestValido_PasaIdYLibroAlUseCase() {

        String id = "LIB-1";
        LibroController.LibroRequest request = crearRequestValido();

        when(libroUseCase.actualizarLibro(
                eq(id),
                any(Libro.class)
        )).thenReturn(libro);

        ArgumentCaptor<Libro> libroCaptor =
                ArgumentCaptor.forClass(Libro.class);


        ResponseEntity<Libro> respuesta =
                libroController.actualizarLibro(id, request);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(libro, respuesta.getBody());

        verify(libroUseCase)
                .actualizarLibro(eq(id), libroCaptor.capture());

        Libro libroEnviado = libroCaptor.getValue();

        assertEquals(request.isbn(), libroEnviado.getIsbn());
        assertEquals(request.titulo(), libroEnviado.getTitulo());
        assertEquals(request.idEditorial(), libroEnviado.getIdEditorial());
        assertEquals(
                List.of("CAT-1", "CAT-2"),
                libroEnviado.getCategorias().stream()
                        .map(Categoria::getIdCategoria)
                        .toList()
        );
        assertEquals(
                List.of("AUT-1", "AUT-2"),
                libroEnviado.getAutores().stream()
                        .map(Autor::getIdAutor)
                        .toList()
        );
    }



    @Test
    void eliminarLibro_LibroExiste_RetornaMensajeYEstado200() {

        String id = "LIB-1";


        ResponseEntity<String> respuesta =
                libroController.eliminarLibro(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Libro eliminado con exito", respuesta.getBody());
        verify(libroUseCase).eliminarLibro(id);
    }

    private Libro crearLibro() {
        Libro nuevoLibro = new Libro();
        nuevoLibro.setIsbn("9780306406157");
        nuevoLibro.setTitulo("Cien años de soledad");
        nuevoLibro.setSubtitulo("Edición de prueba");
        nuevoLibro.setFechaPublicacion(LocalDate.of(1967, 5, 30));
        nuevoLibro.setIdioma("es");
        nuevoLibro.setNumeroPaginas(417);
        nuevoLibro.setDescripcion("Descripción de prueba");
        nuevoLibro.setPortadaUrl("https://ejemplo.com/portada.jpg");
        nuevoLibro.setIdEditorial("EDIT-1");
        return nuevoLibro;
    }

    private LibroController.LibroRequest crearRequestValido() {
        return new LibroController.LibroRequest(
                "9780306406157",
                "Cien años de soledad",
                "Edición de prueba",
                LocalDate.of(1967, 5, 30),
                "es",
                417,
                "Descripción de prueba",
                "https://ejemplo.com/portada.jpg",
                "EDIT-1",
                List.of("CAT-1", "CAT-2"),
                List.of("AUT-1", "AUT-2")
        );
    }
}