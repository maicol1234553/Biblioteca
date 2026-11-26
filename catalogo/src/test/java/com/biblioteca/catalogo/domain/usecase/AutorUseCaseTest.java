package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorUseCaseTest {

    @Mock
    private AutorGateway autorGateway;

    @InjectMocks
    private AutorUseCase autorUseCase;

    private Autor autor;

    @BeforeEach
    void setUp() {
        autor = crearAutorValido();
    }



    @Test
    void guardarAutor_DatosValidosSinId_GeneraIdYGuarda() {

        autor.setIdAutor(null);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.guardarAutor(autor);


        assertSame(autor, resultado);
        assertNotNull(resultado.getIdAutor());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdAutor()));

        verify(autorGateway).buscarAutorPorNombre(autor.getNombre());
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_IdEnBlanco_GeneraIdYGuarda() {

        autor.setIdAutor(" ");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.guardarAutor(autor);


        assertNotNull(resultado.getIdAutor());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdAutor()));
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_IdExistente_LoConserva() {

        autor.setIdAutor("AUTOR-1");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.guardarAutor(autor);


        assertEquals("AUTOR-1", resultado.getIdAutor());
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_NombreDuplicado_LanzaExcepcion() {

        Autor existente = crearAutorValido();
        existente.setIdAutor("AUTOR-2");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(existente);


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> autorUseCase.guardarAutor(autor)
        );


        assertEquals(
                "Ya existe un autor con el nombre: " + autor.getNombre(),
                excepcion.getMessage()
        );
        verify(autorGateway, never()).guardarAutor(any(Autor.class));
    }

    @Test
    void guardarAutor_AutorNulo_LanzaExcepcion() {

        Autor autorNulo = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autorNulo)
        );


        assertEquals("El autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_NombreNulo_LanzaExcepcion() {

        autor.setNombre(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );


        assertEquals("El nombre del autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_NombreEnBlanco_LanzaExcepcion() {

        autor.setNombre(" ");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );


        assertEquals("El nombre del autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_FechaNacimientoFutura_LanzaExcepcion() {

        autor.setFechaNacimiento(LocalDate.now().plusDays(1));


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );


        assertEquals(
                "La fecha de nacimiento no puede ser futura",
                excepcion.getMessage()
        );
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_SinFechaNacimiento_PermiteGuardar() {

        autor.setFechaNacimiento(null);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.guardarAutor(autor);


        assertSame(autor, resultado);
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_FechaNacimientoHoy_PermiteGuardar() {

        autor.setFechaNacimiento(LocalDate.now());

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.guardarAutor(autor);


        assertSame(autor, resultado);
        verify(autorGateway).guardarAutor(autor);
    }



    @Test
    void actualizarAutor_DatosValidos_AsignaIdYActualiza() {

        String idAutor = "AUTOR-1";
        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.actualizarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.actualizarAutor(idAutor, autor);


        assertSame(autor, resultado);
        assertEquals(idAutor, resultado.getIdAutor());

        verify(autorGateway).buscarAutorPorId(idAutor);
        verify(autorGateway).buscarAutorPorNombre(autor.getNombre());
        verify(autorGateway).actualizarAutor(autor);
    }

    @Test
    void actualizarAutor_AutorNoExiste_LanzaExcepcion() {

        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);


        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );


        assertEquals(
                "Autor no encontrado con id: " + idAutor,
                excepcion.getMessage()
        );
        verify(autorGateway, never())
                .actualizarAutor(any(Autor.class));
        verify(autorGateway, never())
                .buscarAutorPorNombre(anyString());
    }

    @Test
    void actualizarAutor_NombreDeOtroAutor_LanzaExcepcion() {

        String idAutor = "AUTOR-1";

        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        Autor otroAutor = crearAutorValido();
        otroAutor.setIdAutor("AUTOR-2");

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(otroAutor);


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );


        assertEquals(
                "Ya existe un autor con el nombre: " + autor.getNombre(),
                excepcion.getMessage()
        );
        verify(autorGateway, never())
                .actualizarAutor(any(Autor.class));
    }

    @Test
    void actualizarAutor_MantieneElMismoNombreDelAutor_PermiteActualizar() {

        String idAutor = "AUTOR-1";

        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        autor.setNombre(autorExistente.getNombre());

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(autorExistente);

        when(autorGateway.actualizarAutor(autor))
                .thenReturn(autor);


        Autor resultado = autorUseCase.actualizarAutor(idAutor, autor);


        assertSame(autor, resultado);
        assertEquals(idAutor, resultado.getIdAutor());
        verify(autorGateway).actualizarAutor(autor);
    }

    @Test
    void actualizarAutor_FechaNacimientoFutura_LanzaExcepcionYSinActualizar() {

        String idAutor = "AUTOR-1";

        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        autor.setFechaNacimiento(LocalDate.now().plusDays(1));

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );


        assertEquals(
                "La fecha de nacimiento no puede ser futura",
                excepcion.getMessage()
        );
        verify(autorGateway, never())
                .actualizarAutor(any(Autor.class));
        verify(autorGateway, never())
                .buscarAutorPorNombre(anyString());
    }



    @Test
    void buscarAutorPorId_AutorExiste_RetornaAutor() {

        when(autorGateway.buscarAutorPorId(autor.getIdAutor()))
                .thenReturn(autor);


        Autor resultado =
                autorUseCase.buscarAutorPorId(autor.getIdAutor());


        assertSame(autor, resultado);
        verify(autorGateway).buscarAutorPorId(autor.getIdAutor());
    }

    @Test
    void buscarAutorPorId_AutorNoExiste_LanzaExcepcion() {

        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);


        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.buscarAutorPorId(idAutor)
        );


        assertEquals(
                "Autor no encontrado con id: " + idAutor,
                excepcion.getMessage()
        );
    }



    @Test
    void listarAutores_ConsultaGateway_RetornaLista() {

        List<Autor> autoresEsperados = List.of(autor);

        when(autorGateway.listarAutores())
                .thenReturn(autoresEsperados);


        List<Autor> resultado = autorUseCase.listarAutores();


        assertSame(autoresEsperados, resultado);
        assertEquals(1, resultado.size());
        assertEquals(autor, resultado.get(0));
        verify(autorGateway).listarAutores();
    }

    @Test
    void listarAutores_NoHayAutores_RetornaListaVacia() {

        when(autorGateway.listarAutores())
                .thenReturn(List.of());


        List<Autor> resultado = autorUseCase.listarAutores();


        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }



    @Test
    void eliminarAutor_AutorExiste_EliminaAutor() {

        String idAutor = autor.getIdAutor();

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autor);


        autorUseCase.eliminarAutor(idAutor);


        verify(autorGateway).buscarAutorPorId(idAutor);
        verify(autorGateway).eliminarAutor(idAutor);
    }

    @Test
    void eliminarAutor_AutorNoExiste_LanzaExcepcionYSinEliminar() {

        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);


        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.eliminarAutor(idAutor)
        );


        assertEquals(
                "Autor no encontrado con id: " + idAutor,
                excepcion.getMessage()
        );
        verify(autorGateway, never()).eliminarAutor(anyString());
    }

    private Autor crearAutorValido() {
        Autor nuevoAutor = new Autor();
        nuevoAutor.setIdAutor("AUTOR-1");
        nuevoAutor.setNombre("Gabriel García Márquez");
        nuevoAutor.setFechaNacimiento(LocalDate.of(1927, 3, 6));
        return nuevoAutor;
    }
}