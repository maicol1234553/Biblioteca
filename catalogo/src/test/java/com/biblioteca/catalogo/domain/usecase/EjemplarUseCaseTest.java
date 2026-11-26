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



    @Test
    void guardarEjemplar_DatosValidosSinId_GeneraIdYGuarda() {

        ejemplar.setIdEjemplar(null);

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);


        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);


        assertSame(ejemplar, resultado);
        assertNotNull(resultado.getIdEjemplar());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdEjemplar()));

        verify(libroGateway).buscarLibroPorId(ejemplar.getIdLibro());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_FechaAdquisicionNula_UsaFechaActual() {

        ejemplar.setFechaAdquisicion(null);

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);


        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);


        assertEquals(LocalDate.now(), resultado.getFechaAdquisicion());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_IdExistente_LoConserva() {

        ejemplar.setIdEjemplar("EJ-1");

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(null);

        when(ejemplarGateway.guardarEjemplar(ejemplar))
                .thenReturn(ejemplar);


        Ejemplar resultado = ejemplarUseCase.guardarEjemplar(ejemplar);


        assertEquals("EJ-1", resultado.getIdEjemplar());
        verify(ejemplarGateway).guardarEjemplar(ejemplar);
    }

    @Test
    void guardarEjemplar_EjemplarNulo_LanzaExcepcion() {

        Ejemplar ejemplarNulo = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplarNulo)
        );


        assertEquals("El ejemplar es obligatorio", excepcion.getMessage());
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_IdLibroNulo_LanzaExcepcion() {

        ejemplar.setIdLibro(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );


        assertEquals(
                "El libro del ejemplar es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_CodigoInventarioEnBlanco_LanzaExcepcion() {

        ejemplar.setCodigoInventario(" ");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );


        assertEquals(
                "El codigo de inventario es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(ejemplarGateway, libroGateway);
    }

    @Test
    void guardarEjemplar_LibroNoExiste_LanzaExcepcion() {

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(null);


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );


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

        Ejemplar existente = crearEjemplarValido();
        existente.setIdEjemplar("EJ-OTRO");

        when(libroGateway.buscarLibroPorId(ejemplar.getIdLibro()))
                .thenReturn(libro);

        when(ejemplarGateway.buscarEjemplarPorCodigoInventario(
                ejemplar.getCodigoInventario()
        )).thenReturn(existente);


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> ejemplarUseCase.guardarEjemplar(ejemplar)
        );


        assertEquals(
                "Ya existe un ejemplar con el codigo de inventario: "
                        + ejemplar.getCodigoInventario(),
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }



    @Test
    void actualizarEjemplar_DatosValidos_ConservaCamposFaltantesYGuarda() {

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


        Ejemplar resultado =
                ejemplarUseCase.actualizarEjemplar(idEjemplar, actualizado);


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

        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);


        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.actualizarEjemplar(idEjemplar, ejemplar)
        );


        assertEquals(
                "Ejemplar no encontrado con id: " + idEjemplar,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }

    @Test
    void actualizarEjemplar_CodigoInventarioYaUsadoPorOtroEjemplar_LanzaExcepcion() {

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


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> ejemplarUseCase.actualizarEjemplar(idEjemplar, ejemplar)
        );


        assertEquals(
                "Ya existe un ejemplar con el codigo de inventario: "
                        + ejemplar.getCodigoInventario(),
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .guardarEjemplar(any(Ejemplar.class));
    }



    @Test
    void buscarEjemplarPorId_EjemplarExiste_RetornaEjemplar() {

        when(ejemplarGateway.buscarEjemplarPorId(ejemplar.getIdEjemplar()))
                .thenReturn(ejemplar);


        Ejemplar resultado =
                ejemplarUseCase.buscarEjemplarPorId(ejemplar.getIdEjemplar());


        assertSame(ejemplar, resultado);
        verify(ejemplarGateway)
                .buscarEjemplarPorId(ejemplar.getIdEjemplar());
    }

    @Test
    void buscarEjemplarPorId_EjemplarNoExiste_LanzaExcepcion() {

        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);


        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.buscarEjemplarPorId(idEjemplar)
        );


        assertEquals(
                "Ejemplar no encontrado con id: " + idEjemplar,
                excepcion.getMessage()
        );
    }



    @Test
    void listarEjemplares_ConsultaGateway_RetornaLista() {

        List<Ejemplar> ejemplaresEsperados = List.of(ejemplar);

        when(ejemplarGateway.listarEjemplares())
                .thenReturn(ejemplaresEsperados);


        List<Ejemplar> resultado = ejemplarUseCase.listarEjemplares();


        assertSame(ejemplaresEsperados, resultado);
        assertEquals(1, resultado.size());
        assertSame(ejemplar, resultado.get(0));
        verify(ejemplarGateway).listarEjemplares();
    }

    @Test
    void listarEjemplares_SinResultados_RetornaListaVacia() {

        when(ejemplarGateway.listarEjemplares())
                .thenReturn(List.of());


        List<Ejemplar> resultado = ejemplarUseCase.listarEjemplares();


        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }



    @Test
    void listarEjemplaresPorLibro_LibroExiste_RetornaEjemplares() {

        String idLibro = "LIB-1";
        List<Ejemplar> ejemplaresEsperados = List.of(ejemplar);

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(libro);

        when(ejemplarGateway.listarEjemplaresPorLibro(idLibro))
                .thenReturn(ejemplaresEsperados);


        List<Ejemplar> resultado =
                ejemplarUseCase.listarEjemplaresPorLibro(idLibro);


        assertSame(ejemplaresEsperados, resultado);
        assertEquals(1, resultado.size());
        verify(libroGateway).buscarLibroPorId(idLibro);
        verify(ejemplarGateway).listarEjemplaresPorLibro(idLibro);
    }

    @Test
    void listarEjemplaresPorLibro_LibroNoExiste_LanzaExcepcion() {

        String idLibro = "LIB-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.listarEjemplaresPorLibro(idLibro)
        );


        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .listarEjemplaresPorLibro(anyString());
    }



    @Test
    void listarEjemplaresDisponibles_ConsultaGateway_RetornaLista() {

        List<Ejemplar> disponiblesEsperados = List.of(ejemplar);

        when(ejemplarGateway.listarEjemplaresDisponibles())
                .thenReturn(disponiblesEsperados);


        List<Ejemplar> resultado =
                ejemplarUseCase.listarEjemplaresDisponibles();


        assertSame(disponiblesEsperados, resultado);
        assertEquals(1, resultado.size());
        verify(ejemplarGateway).listarEjemplaresDisponibles();
    }



    @Test
    void consultarDisponibilidad_LibroNoExiste_LanzaExcepcion() {

        String idLibro = "LIB-INEXISTENTE";

        when(libroGateway.buscarLibroPorId(idLibro))
                .thenReturn(null);


        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarUseCase.consultarDisponibilidad(idLibro)
        );


        assertEquals(
                "Libro no encontrado con id: " + idLibro,
                excepcion.getMessage()
        );
        verify(ejemplarGateway, never())
                .listarEjemplaresPorLibro(anyString());
    }



    @Test
    void eliminarEjemplar_EjemplarExiste_EliminaEjemplar() {

        String idEjemplar = ejemplar.getIdEjemplar();

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(ejemplar);


        ejemplarUseCase.eliminarEjemplar(idEjemplar);


        verify(ejemplarGateway).buscarEjemplarPorId(idEjemplar);
        verify(ejemplarGateway).eliminarEjemplar(idEjemplar);
    }

    @Test
    void eliminarEjemplar_EjemplarNoExiste_LanzaExcepcionYSinEliminar() {

        String idEjemplar = "ID-INEXISTENTE";

        when(ejemplarGateway.buscarEjemplarPorId(idEjemplar))
                .thenReturn(null);


        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarUseCase.eliminarEjemplar(idEjemplar)
        );


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