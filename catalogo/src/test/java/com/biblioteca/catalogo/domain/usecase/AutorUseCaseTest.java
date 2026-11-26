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

    // GUARDAR AUTOR

    @Test
    void guardarAutor_DatosValidosSinId_GeneraIdYGuarda() {
        // Arrange
        autor.setIdAutor(null);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.guardarAutor(autor);

        // Assert
        assertSame(autor, resultado);
        assertNotNull(resultado.getIdAutor());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdAutor()));

        verify(autorGateway).buscarAutorPorNombre(autor.getNombre());
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_IdEnBlanco_GeneraIdYGuarda() {
        // Arrange
        autor.setIdAutor(" ");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.guardarAutor(autor);

        // Assert
        assertNotNull(resultado.getIdAutor());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdAutor()));
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_IdExistente_LoConserva() {
        // Arrange
        autor.setIdAutor("AUTOR-1");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.guardarAutor(autor);

        // Assert
        assertEquals("AUTOR-1", resultado.getIdAutor());
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_NombreDuplicado_LanzaExcepcion() {
        // Arrange
        Autor existente = crearAutorValido();
        existente.setIdAutor("AUTOR-2");

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(existente);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> autorUseCase.guardarAutor(autor)
        );

        // Assert
        assertEquals(
                "Ya existe un autor con el nombre: " + autor.getNombre(),
                excepcion.getMessage()
        );
        verify(autorGateway, never()).guardarAutor(any(Autor.class));
    }

    @Test
    void guardarAutor_AutorNulo_LanzaExcepcion() {
        // Arrange
        Autor autorNulo = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autorNulo)
        );

        // Assert
        assertEquals("El autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_NombreNulo_LanzaExcepcion() {
        // Arrange
        autor.setNombre(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );

        // Assert
        assertEquals("El nombre del autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_NombreEnBlanco_LanzaExcepcion() {
        // Arrange
        autor.setNombre(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );

        // Assert
        assertEquals("El nombre del autor es obligatorio", excepcion.getMessage());
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_FechaNacimientoFutura_LanzaExcepcion() {
        // Arrange
        autor.setFechaNacimiento(LocalDate.now().plusDays(1));

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.guardarAutor(autor)
        );

        // Assert
        assertEquals(
                "La fecha de nacimiento no puede ser futura",
                excepcion.getMessage()
        );
        verifyNoInteractions(autorGateway);
    }

    @Test
    void guardarAutor_SinFechaNacimiento_PermiteGuardar() {
        // Arrange
        autor.setFechaNacimiento(null);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.guardarAutor(autor);

        // Assert
        assertSame(autor, resultado);
        verify(autorGateway).guardarAutor(autor);
    }

    @Test
    void guardarAutor_FechaNacimientoHoy_PermiteGuardar() {
        // Arrange
        autor.setFechaNacimiento(LocalDate.now());

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.guardarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.guardarAutor(autor);

        // Assert
        assertSame(autor, resultado);
        verify(autorGateway).guardarAutor(autor);
    }

    // ACTUALIZAR AUTOR

    @Test
    void actualizarAutor_DatosValidos_AsignaIdYActualiza() {
        // Arrange
        String idAutor = "AUTOR-1";
        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(null);

        when(autorGateway.actualizarAutor(autor))
                .thenReturn(autor);

        // Act
        Autor resultado = autorUseCase.actualizarAutor(idAutor, autor);

        // Assert
        assertSame(autor, resultado);
        assertEquals(idAutor, resultado.getIdAutor());

        verify(autorGateway).buscarAutorPorId(idAutor);
        verify(autorGateway).buscarAutorPorNombre(autor.getNombre());
        verify(autorGateway).actualizarAutor(autor);
    }

    @Test
    void actualizarAutor_AutorNoExiste_LanzaExcepcion() {
        // Arrange
        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);

        // Act
        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );

        // Assert
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
        // Arrange
        String idAutor = "AUTOR-1";

        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        Autor otroAutor = crearAutorValido();
        otroAutor.setIdAutor("AUTOR-2");

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        when(autorGateway.buscarAutorPorNombre(autor.getNombre()))
                .thenReturn(otroAutor);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );

        // Assert
        assertEquals(
                "Ya existe un autor con el nombre: " + autor.getNombre(),
                excepcion.getMessage()
        );
        verify(autorGateway, never())
                .actualizarAutor(any(Autor.class));
    }

    @Test
    void actualizarAutor_MantieneElMismoNombreDelAutor_PermiteActualizar() {
        // Arrange
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

        // Act
        Autor resultado = autorUseCase.actualizarAutor(idAutor, autor);

        // Assert
        assertSame(autor, resultado);
        assertEquals(idAutor, resultado.getIdAutor());
        verify(autorGateway).actualizarAutor(autor);
    }

    @Test
    void actualizarAutor_FechaNacimientoFutura_LanzaExcepcionYSinActualizar() {
        // Arrange
        String idAutor = "AUTOR-1";

        Autor autorExistente = crearAutorValido();
        autorExistente.setIdAutor(idAutor);

        autor.setFechaNacimiento(LocalDate.now().plusDays(1));

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autorExistente);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> autorUseCase.actualizarAutor(idAutor, autor)
        );

        // Assert
        assertEquals(
                "La fecha de nacimiento no puede ser futura",
                excepcion.getMessage()
        );
        verify(autorGateway, never())
                .actualizarAutor(any(Autor.class));
        verify(autorGateway, never())
                .buscarAutorPorNombre(anyString());
    }

    // BUSCAR AUTOR POR ID

    @Test
    void buscarAutorPorId_AutorExiste_RetornaAutor() {
        // Arrange
        when(autorGateway.buscarAutorPorId(autor.getIdAutor()))
                .thenReturn(autor);

        // Act
        Autor resultado =
                autorUseCase.buscarAutorPorId(autor.getIdAutor());

        // Assert
        assertSame(autor, resultado);
        verify(autorGateway).buscarAutorPorId(autor.getIdAutor());
    }

    @Test
    void buscarAutorPorId_AutorNoExiste_LanzaExcepcion() {
        // Arrange
        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);

        // Act
        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.buscarAutorPorId(idAutor)
        );

        // Assert
        assertEquals(
                "Autor no encontrado con id: " + idAutor,
                excepcion.getMessage()
        );
    }

    // LISTAR AUTORES

    @Test
    void listarAutores_ConsultaGateway_RetornaLista() {
        // Arrange
        List<Autor> autoresEsperados = List.of(autor);

        when(autorGateway.listarAutores())
                .thenReturn(autoresEsperados);

        // Act
        List<Autor> resultado = autorUseCase.listarAutores();

        // Assert
        assertSame(autoresEsperados, resultado);
        assertEquals(1, resultado.size());
        assertEquals(autor, resultado.get(0));
        verify(autorGateway).listarAutores();
    }

    @Test
    void listarAutores_NoHayAutores_RetornaListaVacia() {
        // Arrange
        when(autorGateway.listarAutores())
                .thenReturn(List.of());

        // Act
        List<Autor> resultado = autorUseCase.listarAutores();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ELIMINAR AUTOR

    @Test
    void eliminarAutor_AutorExiste_EliminaAutor() {
        // Arrange
        String idAutor = autor.getIdAutor();

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(autor);

        // Act
        autorUseCase.eliminarAutor(idAutor);

        // Assert
        verify(autorGateway).buscarAutorPorId(idAutor);
        verify(autorGateway).eliminarAutor(idAutor);
    }

    @Test
    void eliminarAutor_AutorNoExiste_LanzaExcepcionYSinEliminar() {
        // Arrange
        String idAutor = "ID-INEXISTENTE";

        when(autorGateway.buscarAutorPorId(idAutor))
                .thenReturn(null);

        // Act
        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> autorUseCase.eliminarAutor(idAutor)
        );

        // Assert
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