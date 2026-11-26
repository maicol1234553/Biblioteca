package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.usecase.EjemplarUseCase;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EjemplarControllerTest {

    @Mock
    private EjemplarUseCase ejemplarUseCase;

    @InjectMocks
    private EjemplarController ejemplarController;

    private Ejemplar ejemplar;

    @BeforeEach
    void setUp() {
        ejemplar = crearEjemplar();
    }



    @Test
    void listarEjemplares_RetornaListaYEstado200() {

        List<Ejemplar> esperados = List.of(ejemplar);

        when(ejemplarUseCase.listarEjemplares())
                .thenReturn(esperados);


        ResponseEntity<List<Ejemplar>> respuesta =
                ejemplarController.listarEjemplares();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperados, respuesta.getBody());
        verify(ejemplarUseCase).listarEjemplares();
    }



    @Test
    void listarEjemplaresDisponibles_RetornaListaYEstado200() {

        List<Ejemplar> esperados = List.of(ejemplar);

        when(ejemplarUseCase.listarEjemplaresDisponibles())
                .thenReturn(esperados);


        ResponseEntity<List<Ejemplar>> respuesta =
                ejemplarController.listarEjemplaresDisponibles();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperados, respuesta.getBody());
        verify(ejemplarUseCase).listarEjemplaresDisponibles();
    }



    @Test
    void listarEjemplaresPorLibro_IdValido_RetornaListaYEstado200() {

        String idLibro = "LIB-1";
        List<Ejemplar> esperados = List.of(ejemplar);

        when(ejemplarUseCase.listarEjemplaresPorLibro(idLibro))
                .thenReturn(esperados);


        ResponseEntity<List<Ejemplar>> respuesta =
                ejemplarController.listarEjemplaresPorLibro(idLibro);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperados, respuesta.getBody());
        verify(ejemplarUseCase).listarEjemplaresPorLibro(idLibro);
    }



    @Test
    void buscarEjemplarPorId_EjemplarExiste_RetornaEjemplarYEstado200() {

        String id = "EJ-1";

        when(ejemplarUseCase.buscarEjemplarPorId(id))
                .thenReturn(ejemplar);


        ResponseEntity<Ejemplar> respuesta =
                ejemplarController.buscarEjemplarPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(ejemplar, respuesta.getBody());
        verify(ejemplarUseCase).buscarEjemplarPorId(id);
    }

    @Test
    void buscarEjemplarPorId_EjemplarNoExiste_PropagaExcepcion() {

        String id = "EJ-INEXISTENTE";

        when(ejemplarUseCase.buscarEjemplarPorId(id))
                .thenThrow(new EjemplarNoEncontradoException(
                        "Ejemplar no encontrado con id: " + id
                ));


        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarController.buscarEjemplarPorId(id)
        );


        assertEquals(
                "Ejemplar no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(ejemplarUseCase).buscarEjemplarPorId(id);
    }



    @Test
    void guardarEjemplar_RequestValido_CreaEjemplarYRetornaEstado201() {

        EjemplarController.EjemplarRequest request =
                crearRequestValido();

        when(ejemplarUseCase.guardarEjemplar(any(Ejemplar.class)))
                .thenReturn(ejemplar);

        ArgumentCaptor<Ejemplar> captor =
                ArgumentCaptor.forClass(Ejemplar.class);


        ResponseEntity<Ejemplar> respuesta =
                ejemplarController.guardarEjemplar(request);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(ejemplar, respuesta.getBody());

        verify(ejemplarUseCase).guardarEjemplar(captor.capture());

        Ejemplar enviadoAlUseCase = captor.getValue();

        assertEquals(request.idLibro(), enviadoAlUseCase.getIdLibro());
        assertEquals(
                request.codigoInventario(),
                enviadoAlUseCase.getCodigoInventario()
        );
        assertEquals(request.ubicacion(), enviadoAlUseCase.getUbicacion());
        assertEquals(request.estado(), enviadoAlUseCase.getEstado());
        assertEquals(
                request.fechaAdquisicion(),
                enviadoAlUseCase.getFechaAdquisicion()
        );
        assertEquals(
                request.observaciones(),
                enviadoAlUseCase.getObservaciones()
        );
    }



    @Test
    void actualizarEjemplar_RequestValido_PasaIdYEjemplarAlUseCase() {

        String id = "EJ-1";
        EjemplarController.EjemplarRequest request =
                crearRequestValido();

        when(ejemplarUseCase.actualizarEjemplar(
                eq(id),
                any(Ejemplar.class)
        )).thenReturn(ejemplar);

        ArgumentCaptor<Ejemplar> captor =
                ArgumentCaptor.forClass(Ejemplar.class);


        ResponseEntity<Ejemplar> respuesta =
                ejemplarController.actualizarEjemplar(id, request);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(ejemplar, respuesta.getBody());

        verify(ejemplarUseCase)
                .actualizarEjemplar(eq(id), captor.capture());

        Ejemplar enviadoAlUseCase = captor.getValue();

        assertEquals(request.idLibro(), enviadoAlUseCase.getIdLibro());
        assertEquals(
                request.codigoInventario(),
                enviadoAlUseCase.getCodigoInventario()
        );
        assertEquals(request.ubicacion(), enviadoAlUseCase.getUbicacion());
        assertEquals(request.estado(), enviadoAlUseCase.getEstado());
        assertEquals(
                request.fechaAdquisicion(),
                enviadoAlUseCase.getFechaAdquisicion()
        );
        assertEquals(
                request.observaciones(),
                enviadoAlUseCase.getObservaciones()
        );
    }



    @Test
    void eliminarEjemplar_EjemplarExiste_RetornaMensajeYEstado200() {

        String id = "EJ-1";


        ResponseEntity<String> respuesta =
                ejemplarController.eliminarEjemplar(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Ejemplar eliminado con exito", respuesta.getBody());
        verify(ejemplarUseCase).eliminarEjemplar(id);
    }

    @Test
    void eliminarEjemplar_EjemplarNoExiste_PropagaExcepcion() {

        String id = "EJ-INEXISTENTE";

        doThrow(new EjemplarNoEncontradoException(
                "Ejemplar no encontrado con id: " + id
        )).when(ejemplarUseCase).eliminarEjemplar(id);


        EjemplarNoEncontradoException excepcion = assertThrows(
                EjemplarNoEncontradoException.class,
                () -> ejemplarController.eliminarEjemplar(id)
        );


        assertEquals(
                "Ejemplar no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(ejemplarUseCase).eliminarEjemplar(id);
    }

    private Ejemplar crearEjemplar() {
        Ejemplar nuevoEjemplar = new Ejemplar();
        nuevoEjemplar.setIdEjemplar("EJ-1");
        nuevoEjemplar.setIdLibro("LIB-1");
        nuevoEjemplar.setCodigoInventario("INV-001");
        nuevoEjemplar.setUbicacion("Estante A");
        nuevoEjemplar.setEstado("DISPONIBLE");
        nuevoEjemplar.setFechaAdquisicion(LocalDate.of(2024, 1, 15));
        nuevoEjemplar.setObservaciones("Buen estado");
        return nuevoEjemplar;
    }

    private EjemplarController.EjemplarRequest crearRequestValido() {
        return new EjemplarController.EjemplarRequest(
                "LIB-1",
                "INV-001",
                "Estante A",
                "DISPONIBLE",
                LocalDate.of(2024, 1, 15),
                "Buen estado"
        );
    }
}