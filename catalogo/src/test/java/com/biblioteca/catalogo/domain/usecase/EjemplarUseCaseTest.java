package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.DisponibilidadLibro;
import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.model.EstadoEjemplar;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.gateway.EjemplarGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class EjemplarUseCaseTest {

    @Mock
    private EjemplarGateway ejemplarGateway;

    @Mock
    private LibroGateway libroGateway;

    @InjectMocks
    private EjemplarUseCase ejemplarUseCase;

    private Ejemplar ejemplar;
    private Libro libro;

    @BeforeEach
    void setUp() {
        ejemplar = crearEjemplarValido();
        libro = crearLibroValido();
    }

    // GUARDAR EJEMPLAR

    @Test
    void guardarEjemplar_DatosValidosSinId_GeneraIdYGuarda() {
        // Arrange
        ejemplar.setIdEjemplar(null);

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);

        // Act
        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);

        // Assert
        assertSame(ejemplar, resultado);
        assertNotNull(resultado.getIdEjemplar());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdEjemplar()));

        verify(libroGateway).buscarLibroPorId(ejemplar.getIdLibro());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_FechaAdquisicionNula_UsaFechaActual() {
        // Arrange
        ejemplar.setFechaAdquisicion(null);

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);

        // Act
        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);

        // Assert
        assertEquals(LocalDate.now(), resultado.getFechaAdquisicion());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_IdExistente_LoConserva() {
        // Arrange
        ejemplar.setIdEjemplar("EJ-1");

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);

        // Act
        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);

        // Assert
        assertEquals("EJ-1", resultado.getIdEjemplar());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_EjemplarNulo_LanzaExcepcion() {
        // Arrange
        Ejemplar ejemplarNulo = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplarNulo)
        );

        // Assert
        assertEquals("El ejemplar es obligatorio", excepcion.getMessage());
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_IdLibroNulo_LanzaExcepcion() {
        // Arrange
        ejemplar.setIdLibro(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );

        // Assert
        assertEquals(
                "El libro del ejemplar es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_CodigoInventarioEnBlanco_LanzaExcepcion() {
        // Arrange
        ejemplar.setCodigoInventario(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );

        // Assert
        assertEquals(
                "El codigo de inventario es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_LibroNoExiste_LanzaExcepcion() {
        // Arrange
        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + ejemplar.getIdLibro(),
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
        verify(ejemplarGateway, never())
                .buscarEjemplarPorCodigoInventario(anyString());
    }

    @Test
    void guardarEjemplar_CodigoInventarioDuplicado_LanzaExcepcion() {
        // Arrange
        Ejemplar existente = crearEjemplarValido();
        existente.setIdEjemplar("EJ-OTRO");

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(existente);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );

        // Assert
        assertEquals(
                "Ya existe un ejemplar con el codigo de inventario: "
                        + ejemplar.getCodigoInventario(),
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }

    // ACTUALIZAR EJEMPLAR

    @Test
    void actualizarEjemplar_DatosValidos_ConservaCamposFaltantesYGuarda() {
        // Arrange
        String idEjemplar = "EJ-1";

        Ejemplar existente = crearEjemplarValido();
        existente.setIdEjemplar(idEjemplar);
        existente.setObservaciones("Observación anterior");

        Ejemplar actualizado = new Ejemplar();
        actualizado.setIdLibro(" ");
        actualizado.setCodigoInventario("");
        actualizado.setUbicacion(null);
        actualizado.setEstado(" ");
        actualizado.setFechaAdquisicion(null);
        actualizado.setObservaciones(null);

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorId(existente.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                existente.getCodigoInventario()
        )).thenReturn(existente);

        when(ejemplarGateway.guardarEjemplar(actualizado))
                .thenReturn(actualizado);

        // Act
        Ejemplar resultado =
                ejemplarUseCase.actualizarEjemplar(idEjemplar, actualizado);

        // Assert
        assertSame(actualizado, resultado);
        assertEquals(idEjemplar, resultado.getIdEjemplar());
        assertEquals(existente.getIdLibro(), resultado.getIdLibro());
        assertEquals(
                existente.getCodigoInventario(),
                resultado.getCodigoInventario()
        );
        assertEquals(existente.getUbicacion(), resultado.getUbicacion());
        assertEquals(existente.getEstado(), resultado.getEstado());
        assertEquals(
                existente.getFechaAdquisicion(),
                resultado.getFechaAdquisicion()
        );
        assertEquals(
                existente.getObservaciones(),
                resultado.getObservaciones()
        );

        verify(ejemplarGateway).buscarEjemplarPorId(idEjemplar);
        verify(ejemplarGateway).guardarEjemplar(actualizado);
    }

    @Test
    void actualizarEjemplar_EjemplarNoExiste_LanzaExcepcion() {
        // Arrange
        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);

        // Act
        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.actualizarEjemplar(idEjemplar, ejemplar)
        );

        // Assert
        assertEquals(
                "Ejemplar no encontrado con id: " + idEjemplar,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }

    @Test
    void actualizarEjemplar_CodigoInventarioYaUsadoPorOtroEjemplar_LanzaExcepcion() {
        // Arrange
        String idEjemplar = "EJ-1";

        Ejemplar existente = crearEjemplarValido();
        existente.setIdEjemplar(idEjemplar);

        Ejemplar otroEjemplar = crearEjemplarValido();
        otroEjemplar.setIdEjemplar("EJ-2");

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(existente);

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(otroEjemplar);

        // Act
        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> ejemplarUseCase.actualizarEjemplar(idEjemplar, ejemplar)
        );

        // Assert
        assertEquals(
                "Ya existe un ejemplar con el codigo de inventario: "
                        + ejemplar.getCodigoInventario(),
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }

    // BUSCAR EJEMPLAR POR ID

    @Test
    void buscarEjemplarPorId_EjemplarExiste_RetornaEjemplar() {
        // Arrange
        when(ejemplarGateway.buscarEjemplarPorId(ejemplar.getIdEjemplar()))
                .thenReturn(ejemplar);

        // Act
        Ejemplar resultado =
                ejemplarUseCase.buscarEjemplarPorId(ejemplar.getIdEjemplar());

        // Assert
        assertSame(ejemplar, resultado);
        verify(ejemplarGateway)
                .buscarEjemplarPorId(ejemplar.getIdEjemplar());
    }

    @Test
    void buscarEjemplarPorId_EjemplarNoExiste_LanzaExcepcion() {
        // Arrange
        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);

        // Act
        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.buscarEjemplarPorId(idEjemplar)
        );

        // Assert
        assertEquals(
                "Ejemplar no encontrado con id: " + idEjemplar,
                excepcion.getMessage()
        );
    }

    // LISTAR EJEMPLARES

    @Test
    void listarEjemplares_ConsultaGateway_RetornaLista() {
        // Arrange
        List<Ejemplar> ejemplaresEsperados = List.of(ejemplar);

        when(ejemplarGateway.listarEjemplares())
                .thenReturn(ejemplaresEsperados);

        // Act
        List<Ejemplar> resultado = ejemplarUseCase.listarEjemplares();

        // Assert
        assertSame(ejemplaresEsperados, resultado);
        assertEquals(1, resultado.size());
        assertSame(ejemplar, resultado.get(0));
        verify(ejemplarGateway).listarEjemplares();
    }

    @Test
    void listarEjemplares_SinResultados_RetornaListaVacia() {
        // Arrange
        when(ejemplarGateway.listarEjemplares())
                .thenReturn(List.of());

        // Act
        List<Ejemplar> resultado = ejemplarUseCase.listarEjemplares();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // LISTAR EJEMPLARES POR LIBRO

    @Test
    void listarEjemplaresPorLibro_LibroExiste_RetornaEjemplares() {
        // Arrange
        String idLibro = "LIB-1";
        List<Ejemplar> ejemplaresEsperados = List.of(ejemplar);

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(libro);

        when(ejemplarGateway.listarEjemplaresPorLibro(idLibro))
                .thenReturn(ejemplaresEsperados);

        // Act
        List<Ejemplar> resultado =
                ejemplarUseCase.listarEjemplaresPorLibro(idLibro);

        // Assert
        assertSame(ejemplaresEsperados, resultado);
        assertEquals(1, resultado.size());
        verify(libroGateway).buscarLibroPorId(idLibro);
        verify(ejemplarGateway).listarEjemplaresPorLibro(idLibro);
    }

    @Test
    void listarEjemplaresPorLibro_LibroNoExiste_LanzaExcepcion() {
        // Arrange
        String idLibro = "LIB-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.listarEjemplaresPorLibro(idLibro)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .listarEjemplaresPorLibro(anyString());
    }

    // LISTAR EJEMPLARES DISPONIBLES

    @Test
    void listarEjemplaresDisponibles_ConsultaGateway_RetornaLista() {
        // Arrange
        List<Ejemplar> disponiblesEsperados = List.of(ejemplar);

        when(ejemplarGateway.listarEjemplaresDisponibles())
                .thenReturn(disponiblesEsperados);

        // Act
        List<Ejemplar> resultado =
                ejemplarUseCase.listarEjemplaresDisponibles();

        // Assert
        assertSame(disponiblesEsperados, resultado);
        assertEquals(1, resultado.size());
        verify(ejemplarGateway).listarEjemplaresDisponibles();
    }

    // CONSULTAR DISPONIBILIDAD

    @Test
    void consultarDisponibilidad_LibroNoExiste_LanzaExcepcion() {
        // Arrange
        String idLibro = "LIB-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.consultarDisponibilidad(idLibro)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .listarEjemplaresPorLibro(anyString());
    }

    // ELIMINAR EJEMPLAR

    @Test
    void eliminarEjemplar_EjemplarExiste_EliminaEjemplar() {
        // Arrange
        String idEjemplar = ejemplar.getIdEjemplar();

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(ejemplar);

        // Act
        ejemplarUseCase.eliminarEjemplar(idEjemplar);

        // Assert
        verify(ejemplarGateway).buscarEjemplarPorId(idEjemplar);
        verify(ejemplarGateway).eliminarEjemplar(idEjemplar);
    }

    @Test
    void eliminarEjemplar_EjemplarNoExiste_LanzaExcepcionYSinEliminar() {
        // Arrange
        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);

        // Act
        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.eliminarEjemplar(idEjemplar)
        );

        // Assert
        assertEquals(
                "Ejemplar no encontrado con id: " + idEjemplar,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .eliminarEjemplar(anyString());
    }

    private Ejemplar crearEjemplarValido() {
        Ejemplar nuevoEjemplar = new Ejemplar();
        nuevoEjemplar.setIdEjemplar("EJ-1");
        nuevoEjemplar.setIdLibro("LIB-1");
        nuevoEjemplar.setCodigoInventario("INV-001");
        nuevoEjemplar.setUbicacion("Estante A");
        nuevoEjemplar.setEstado(EstadoEjemplar.DISPONIBLE.name());
        nuevoEjemplar.setFechaAdquisicion(LocalDate.of(2024, 1, 15));
        nuevoEjemplar.setObservaciones("Buen estado");
        return nuevoEjemplar;
    }

    private Ejemplar crearEjemplarConEstado(
            String idEjemplar,
            String estado,
            String ubicacion
    ) {
        Ejemplar nuevoEjemplar = crearEjemplarValido();
        nuevoEjemplar.setIdEjemplar(idEjemplar);
        nuevoEjemplar.setEstado(estado);
        nuevoEjemplar.setUbicacion(ubicacion);
        return nuevoEjemplar;
    }

    private Libro crearLibroValido() {
        Libro nuevoLibro = new Libro();
        nuevoLibro.setIdLibro("LIB-1");
        nuevoLibro.setTitulo("Libro de prueba");
        return nuevoLibro;
    }
}