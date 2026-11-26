package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.LibroExterno;
import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroExternoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoExternoUseCaseTest {

    @Mock
    private LibroExternoGateway libroExternoGateway;

    @Mock
    private LibroUseCase libroUseCase;

    @Mock
    private EditorialGateway editorialGateway;

    @Mock
    private AutorGateway autorGateway;

    @Mock
    private CategoriaGateway categoriaGateway;

    @InjectMocks
    private CatalogoExternoUseCase catalogoExternoUseCase;

    private LibroExterno libroExterno;
    private Editorial editorial;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        libroExterno = crearLibroExterno();

        editorial = new Editorial();
        editorial.setIdEditorial("EDITORIAL-1");
        editorial.setNombre("Editorial Uno");

        categoria = new Categoria();
        categoria.setIdCategoria("CATEGORIA-1");
        categoria.setNombre("Ficción");
        categoria.setDescripcion("Libros de ficción");
    }



    @Test
    void buscar_ConsultaValida_RecortaEspaciosYRetornaResultados() {

        String consulta = "  Gabriel García Márquez  ";
        List<LibroExterno> resultadosEsperados = List.of(libroExterno);

        when(libroExternoGateway.buscar("Gabriel García Márquez"))
                .thenReturn(resultadosEsperados);


        List<LibroExterno> resultado =
                catalogoExternoUseCase.buscar(consulta);


        assertSame(resultadosEsperados, resultado);
        verify(libroExternoGateway).buscar("Gabriel García Márquez");
    }

    @Test
    void buscar_ConsultaNula_LanzaExcepcion() {

        String consulta = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.buscar(consulta)
        );


        assertEquals(
                "El termino de busqueda es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroExternoGateway);
    }

    @Test
    void buscar_ConsultaEnBlanco_LanzaExcepcion() {

        String consulta = "   ";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.buscar(consulta)
        );


        assertEquals(
                "El termino de busqueda es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroExternoGateway);
    }



    @Test
    void buscarPorIsbn_IsbnConGuiones_NormalizaYRetornaLibro() {

        String isbnConGuiones = "978-0-306-40615-7";
        String isbnNormalizado = "9780306406157";

        when(libroExternoGateway.buscarPorIsbn(isbnNormalizado))
                .thenReturn(Optional.of(libroExterno));


        LibroExterno resultado =
                catalogoExternoUseCase.buscarPorIsbn(isbnConGuiones);


        assertSame(libroExterno, resultado);
        verify(libroExternoGateway).buscarPorIsbn(isbnNormalizado);
    }

    @Test
    void buscarPorIsbn_IsbnConEspaciosYGuiones_LimpiaElFormato() {

        String isbn = "  978-0-306-40615-7  ";
        String isbnNormalizado = "9780306406157";

        when(libroExternoGateway.buscarPorIsbn(isbnNormalizado))
                .thenReturn(Optional.of(libroExterno));


        LibroExterno resultado =
                catalogoExternoUseCase.buscarPorIsbn(isbn);


        assertSame(libroExterno, resultado);
        verify(libroExternoGateway).buscarPorIsbn(isbnNormalizado);
    }

    @Test
    void buscarPorIsbn_IsbnNulo_LanzaExcepcion() {

        String isbn = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.buscarPorIsbn(isbn)
        );


        assertEquals("El ISBN es obligatorio", excepcion.getMessage());
        verifyNoInteractions(libroExternoGateway);
    }

    @Test
    void buscarPorIsbn_IsbnEnBlanco_LanzaExcepcion() {

        String isbn = " ";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.buscarPorIsbn(isbn)
        );


        assertEquals("El ISBN es obligatorio", excepcion.getMessage());
        verifyNoInteractions(libroExternoGateway);
    }

    @Test
    void buscarPorIsbn_FormatoInvalido_LanzaExcepcion() {

        String isbn = "ISBN-invalido";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.buscarPorIsbn(isbn)
        );


        assertEquals(
                "El ISBN no tiene un formato valido",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroExternoGateway);
    }

    @Test
    void buscarPorIsbn_NoHayResultadosExternos_LanzaExcepcion() {

        String isbn = "9780306406157";

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.empty());


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> catalogoExternoUseCase.buscarPorIsbn(isbn)
        );


        assertEquals(
                "No se encontro informacion externa para el ISBN: " + isbn,
                excepcion.getMessage()
        );
        verify(libroExternoGateway).buscarPorIsbn(isbn);
    }



    @Test
    void importarPorIsbn_DatosExternosValidos_MapeaYGuardaLibro() {

        String isbnConGuiones = "978-0-306-40615-7";
        String isbnNormalizado = "9780306406157";

        Autor autorUno = new Autor();
        autorUno.setIdAutor("AUTOR-1");
        autorUno.setNombre("Autor Uno");

        Autor autorDosGuardado = new Autor();
        autorDosGuardado.setIdAutor("AUTOR-2");
        autorDosGuardado.setNombre("Autor Dos");

        libroExterno.setAutores(
                List.of("  Autor Uno  ", " ", "Autor Dos")
        );

        when(libroExternoGateway.buscarPorIsbn(isbnNormalizado))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(autorGateway.buscarAutorPorNombre("Autor Uno"))
                .thenReturn(autorUno);

        when(autorGateway.buscarAutorPorNombre("Autor Dos"))
                .thenReturn(null);

        when(autorGateway.guardarAutor(any(Autor.class)))
                .thenReturn(autorDosGuardado);

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbnConGuiones,
                "CATEGORIA-1"
        );


        assertNotNull(resultado);
        assertEquals(isbnNormalizado, resultado.getIsbn());
        assertEquals(libroExterno.getTitulo(), resultado.getTitulo());
        assertEquals(libroExterno.getSubtitulo(), resultado.getSubtitulo());
        assertEquals(libroExterno.getIdioma(), resultado.getIdioma());
        assertEquals(libroExterno.getNumeroPaginas(), resultado.getNumeroPaginas());
        assertEquals(libroExterno.getDescripcion(), resultado.getDescripcion());
        assertEquals(libroExterno.getPortadaUrl(), resultado.getPortadaUrl());

        assertEquals("EDITORIAL-1", resultado.getIdEditorial());
        assertEquals("Editorial Uno", resultado.getNombreEditorial());

        assertEquals(1, resultado.getCategorias().size());
        assertSame(categoria, resultado.getCategorias().get(0));

        assertEquals(2, resultado.getAutores().size());
        assertSame(autorUno, resultado.getAutores().get(0));
        assertSame(autorDosGuardado, resultado.getAutores().get(1));

        verify(libroUseCase).guardarLibro(any(Libro.class));
        verify(autorGateway).guardarAutor(any(Autor.class));
    }

    @Test
    void importarPorIsbn_IsbnNulo_LanzaExcepcionAntesDeBuscar() {

        String isbn = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.importarPorIsbn(
                        isbn,
                        "CATEGORIA-1"
                )
        );


        assertEquals("El ISBN es obligatorio", excepcion.getMessage());
        verifyNoInteractions(
                libroExternoGateway,
                libroUseCase,
                editorialGateway,
                autorGateway,
                categoriaGateway
        );
    }

    @Test
    void importarPorIsbn_IsbnInvalido_LanzaExcepcionAntesDeBuscar() {

        String isbn = "ABC-123";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> catalogoExternoUseCase.importarPorIsbn(
                        isbn,
                        "CATEGORIA-1"
                )
        );


        assertEquals(
                "El ISBN no tiene un formato valido",
                excepcion.getMessage()
        );
        verifyNoInteractions(libroExternoGateway, libroUseCase);
    }

    @Test
    void importarPorIsbn_LibroExternoNoExiste_LanzaExcepcion() {

        String isbn = "9780306406157";

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.empty());


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> catalogoExternoUseCase.importarPorIsbn(
                        isbn,
                        "CATEGORIA-1"
                )
        );


        assertEquals(
                "No se encontro informacion externa para el ISBN: " + isbn,
                excepcion.getMessage()
        );
        verifyNoInteractions(
                libroUseCase,
                editorialGateway,
                autorGateway,
                categoriaGateway
        );
    }

    @Test
    void importarPorIsbn_CategoriaIndicadaNoExiste_LanzaExcepcion() {

        String isbn = "9780306406157";

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorId("ID-INEXISTENTE"))
                .thenReturn(null);


        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> catalogoExternoUseCase.importarPorIsbn(
                        isbn,
                        "ID-INEXISTENTE"
                )
        );


        assertEquals(
                "Categoria no encontrada con id: ID-INEXISTENTE",
                excepcion.getMessage()
        );
        verify(libroUseCase, never()).guardarLibro(any(Libro.class));
        verifyNoInteractions(autorGateway);
    }

    @Test
    void importarPorIsbn_EditorialNoExiste_CreaYGuardaEditorial() {

        String isbn = "9780306406157";

        libroExterno.setAutores(null);

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(any(Editorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Editorial> editorialCaptor =
                ArgumentCaptor.forClass(Editorial.class);


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbn,
                "CATEGORIA-1"
        );


        verify(editorialGateway).guardarEditorial(editorialCaptor.capture());

        Editorial editorialGuardada = editorialCaptor.getValue();

        assertNotNull(editorialGuardada.getIdEditorial());
        assertDoesNotThrow(
                () -> UUID.fromString(editorialGuardada.getIdEditorial())
        );
        assertEquals("Editorial Uno", editorialGuardada.getNombre());
        assertEquals(
                editorialGuardada.getIdEditorial(),
                resultado.getIdEditorial()
        );
        verify(libroUseCase).guardarLibro(any(Libro.class));
    }

    @Test
    void importarPorIsbn_EditorialExternaEnBlanco_UsaSinEditorial() {

        String isbn = "9780306406157";
        libroExterno.setEditorial(" ");

        Editorial editorialSinNombre = new Editorial();
        editorialSinNombre.setIdEditorial("EDITORIAL-SIN-NOMBRE");
        editorialSinNombre.setNombre("Sin editorial");

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Sin editorial"))
                .thenReturn(editorialSinNombre);

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(autorGateway.buscarAutorPorNombre("Autor Uno"))
                .thenReturn(new Autor());

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbn,
                "CATEGORIA-1"
        );


        assertEquals(
                "EDITORIAL-SIN-NOMBRE",
                resultado.getIdEditorial()
        );
        assertEquals("Sin editorial", resultado.getNombreEditorial());
        verify(editorialGateway)
                .buscarEditorialPorNombre("Sin editorial");
    }

    @Test
    void importarPorIsbn_SinIdCategoriaYGeneralExiste_UsaCategoriaGeneral() {

        String isbn = "9780306406157";

        Categoria general = new Categoria();
        general.setIdCategoria("CAT-GENERAL");
        general.setNombre("General");

        libroExterno.setAutores(null);

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorNombre("General"))
                .thenReturn(general);

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(isbn, null);


        assertEquals(1, resultado.getCategorias().size());
        assertSame(general, resultado.getCategorias().get(0));

        verify(categoriaGateway).buscarCategoriaPorNombre("General");
        verify(categoriaGateway, never())
                .guardarCategoria(any(Categoria.class));
    }

    @Test
    void importarPorIsbn_SinIdCategoriaYGeneralNoExiste_CreaCategoriaGeneral() {

        String isbn = "9780306406157";
        libroExterno.setAutores(null);

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorNombre("General"))
                .thenReturn(null);

        when(categoriaGateway.guardarCategoria(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Categoria> categoriaCaptor =
                ArgumentCaptor.forClass(Categoria.class);


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(isbn, " ");


        verify(categoriaGateway).guardarCategoria(categoriaCaptor.capture());

        Categoria categoriaGuardada = categoriaCaptor.getValue();

        assertNotNull(categoriaGuardada.getIdCategoria());
        assertDoesNotThrow(
                () -> UUID.fromString(categoriaGuardada.getIdCategoria())
        );
        assertEquals("General", categoriaGuardada.getNombre());
        assertEquals(
                "Categoria asignada automaticamente al importar libros externos",
                categoriaGuardada.getDescripcion()
        );
        assertSame(categoriaGuardada, resultado.getCategorias().get(0));
    }

    @Test
    void importarPorIsbn_ListaDeAutoresNula_ImportaLibroSinAutores() {

        String isbn = "9780306406157";
        libroExterno.setAutores(null);

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbn,
                "CATEGORIA-1"
        );


        assertNotNull(resultado.getAutores());
        assertTrue(resultado.getAutores().isEmpty());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void importarPorIsbn_AutorNoExiste_CreaYGuardaAutor() {

        String isbn = "9780306406157";
        libroExterno.setAutores(List.of("  Autor Nuevo  "));

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(autorGateway.buscarAutorPorNombre("Autor Nuevo"))
                .thenReturn(null);

        when(autorGateway.guardarAutor(any(Autor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Autor> autorCaptor =
                ArgumentCaptor.forClass(Autor.class);


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbn,
                "CATEGORIA-1"
        );


        verify(autorGateway).guardarAutor(autorCaptor.capture());

        Autor autorGuardado = autorCaptor.getValue();

        assertNotNull(autorGuardado.getIdAutor());
        assertDoesNotThrow(
                () -> UUID.fromString(autorGuardado.getIdAutor())
        );
        assertEquals("Autor Nuevo", autorGuardado.getNombre());

        assertEquals(1, resultado.getAutores().size());
        assertSame(autorGuardado, resultado.getAutores().get(0));
    }

    @Test
    void importarPorIsbn_NombreDeAutorEnBlanco_OmiteEseAutor() {

        String isbn = "9780306406157";
        libroExterno.setAutores(List.of(" ", "\t"));

        when(libroExternoGateway.buscarPorIsbn(isbn))
                .thenReturn(Optional.of(libroExterno));

        when(editorialGateway.buscarEditorialPorNombre("Editorial Uno"))
                .thenReturn(editorial);

        when(categoriaGateway.buscarCategoriaPorId("CATEGORIA-1"))
                .thenReturn(categoria);

        when(libroUseCase.guardarLibro(any(Libro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Libro resultado = catalogoExternoUseCase.importarPorIsbn(
                isbn,
                "CATEGORIA-1"
        );


        assertTrue(resultado.getAutores().isEmpty());
        verifyNoInteractions(autorGateway);
    }

    private LibroExterno crearLibroExterno() {
        LibroExterno libro = new LibroExterno();
        libro.setTitulo("Cien años de soledad");
        libro.setSubtitulo("Edición de prueba");
        libro.setIdioma("es");
        libro.setNumeroPaginas(417);
        libro.setDescripcion("Descripción del libro");
        libro.setPortadaUrl("https://ejemplo.com/portada.jpg");
        libro.setEditorial("Editorial Uno");
        libro.setAutores(List.of("Autor Uno"));
        return libro;
    }
}