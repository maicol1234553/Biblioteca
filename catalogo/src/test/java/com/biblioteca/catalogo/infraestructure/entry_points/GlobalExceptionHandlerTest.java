package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }



    @Test
    void noEncontrado_AutorNoEncontrado_Retorna404YMensaje() {

        AutorNoEncontradoException excepcion =
                new AutorNoEncontradoException("Autor no encontrado");


        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Autor no encontrado", respuesta.getBody());
    }

    @Test
    void noEncontrado_CategoriaNoEncontrada_Retorna404() {

        CategoriaNoEncontradaException excepcion =
                new CategoriaNoEncontradaException("Categoría no encontrada");


        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Categoría no encontrada", respuesta.getBody());
    }

    @Test
    void noEncontrado_EditorialNoEncontrada_Retorna404() {

        EditorialNoEncontradaException excepcion =
                new EditorialNoEncontradaException("Editorial no encontrada");


        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Editorial no encontrada", respuesta.getBody());
    }

    @Test
    void noEncontrado_LibroNoEncontrado_Retorna404() {

        LibroNoEncontradoException excepcion =
                new LibroNoEncontradoException("Libro no encontrado");


        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Libro no encontrado", respuesta.getBody());
    }

    @Test
    void noEncontrado_EjemplarNoEncontrado_Retorna404() {

        EjemplarNoEncontradoException excepcion =
                new EjemplarNoEncontradoException("Ejemplar no encontrado");


        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ejemplar no encontrado", respuesta.getBody());
    }



    @Test
    void conflicto_RecursoDuplicado_Retorna409YMensaje() {

        RecursoDuplicadoException excepcion =
                new RecursoDuplicadoException("El recurso ya existe");


        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);


        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El recurso ya existe", respuesta.getBody());
    }

    @Test
    void conflicto_RecursoEnUso_Retorna409() {

        RecursoEnUsoException excepcion =
                new RecursoEnUsoException("El recurso está en uso");


        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);


        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El recurso está en uso", respuesta.getBody());
    }

    @Test
    void conflicto_IntegridadDeDatos_Retorna409() {

        DataIntegrityViolationException excepcion =
                new DataIntegrityViolationException("Conflicto de integridad");


        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);


        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("Conflicto de integridad", respuesta.getBody());
    }



    @Test
    void solicitudInvalida_ArgumentoInvalido_Retorna400YMensaje() {

        IllegalArgumentException excepcion =
                new IllegalArgumentException("El nombre es obligatorio");


        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El nombre es obligatorio", respuesta.getBody());
    }

    @Test
    void solicitudInvalida_JsonMalFormado_Retorna400YMensajeGenerico() {

        HttpMessageNotReadableException excepcion =
                mock(HttpMessageNotReadableException.class);


        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody()
        );
    }

    @Test
    void solicitudInvalida_TipoDeParametroIncorrecto_Retorna400ConNombreParametro() {

        MethodArgumentTypeMismatchException excepcion =
                mock(MethodArgumentTypeMismatchException.class);

        when(excepcion.getName()).thenReturn("id");


        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Parametro invalido: id", respuesta.getBody());
    }

    @Test
    void solicitudInvalida_MensajeNulo_UsaMensajeGenerico() {

        IllegalArgumentException excepcion =
                new IllegalArgumentException((String) null);


        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }

    @Test
    void solicitudInvalida_MensajeEnBlanco_UsaMensajeGenerico() {

        IllegalArgumentException excepcion =
                new IllegalArgumentException(" ");


        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }



    @Test
    void errorInesperado_ExcepcionConMensaje_Retorna500YMensaje() {

        Exception excepcion = new Exception("Fallo inesperado");


        ResponseEntity<String> respuesta =
                exceptionHandler.errorInesperado(excepcion);


        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );
        assertEquals("Ocurrio un error: Fallo inesperado", respuesta.getBody());
    }

    @Test
    void errorInesperado_MensajeEnBlanco_UsaMensajeGenerico() {

        Exception excepcion = new Exception(" ");


        ResponseEntity<String> respuesta =
                exceptionHandler.errorInesperado(excepcion);


        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );
        assertEquals(
                "Ocurrio un error: Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }
}