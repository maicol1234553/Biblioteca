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

    // RECURSO NO ENCONTRADO: HTTP 404

    @Test
    void noEncontrado_AutorNoEncontrado_Retorna404YMensaje() {
        // Arrange
        AutorNoEncontradoException excepcion =
                new AutorNoEncontradoException("Autor no encontrado");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Autor no encontrado", respuesta.getBody());
    }

    @Test
    void noEncontrado_CategoriaNoEncontrada_Retorna404() {
        // Arrange
        CategoriaNoEncontradaException excepcion =
                new CategoriaNoEncontradaException("Categoría no encontrada");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Categoría no encontrada", respuesta.getBody());
    }

    @Test
    void noEncontrado_EditorialNoEncontrada_Retorna404() {
        // Arrange
        EditorialNoEncontradaException excepcion =
                new EditorialNoEncontradaException("Editorial no encontrada");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Editorial no encontrada", respuesta.getBody());
    }

    @Test
    void noEncontrado_LibroNoEncontrado_Retorna404() {
        // Arrange
        LibroNoEncontradoException excepcion =
                new LibroNoEncontradoException("Libro no encontrado");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Libro no encontrado", respuesta.getBody());
    }

    @Test
    void noEncontrado_EjemplarNoEncontrado_Retorna404() {
        // Arrange
        EjemplarNoEncontradoException excepcion =
                new EjemplarNoEncontradoException("Ejemplar no encontrado");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.noEncontrado(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ejemplar no encontrado", respuesta.getBody());
    }

    // CONFLICTO: HTTP 409

    @Test
    void conflicto_RecursoDuplicado_Retorna409YMensaje() {
        // Arrange
        RecursoDuplicadoException excepcion =
                new RecursoDuplicadoException("El recurso ya existe");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El recurso ya existe", respuesta.getBody());
    }

    @Test
    void conflicto_RecursoEnUso_Retorna409() {
        // Arrange
        RecursoEnUsoException excepcion =
                new RecursoEnUsoException("El recurso está en uso");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("El recurso está en uso", respuesta.getBody());
    }

    @Test
    void conflicto_IntegridadDeDatos_Retorna409() {
        // Arrange
        DataIntegrityViolationException excepcion =
                new DataIntegrityViolationException("Conflicto de integridad");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.conflicto(excepcion);

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("Conflicto de integridad", respuesta.getBody());
    }

    // SOLICITUD INVÁLIDA: HTTP 400

    @Test
    void solicitudInvalida_ArgumentoInvalido_Retorna400YMensaje() {
        // Arrange
        IllegalArgumentException excepcion =
                new IllegalArgumentException("El nombre es obligatorio");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El nombre es obligatorio", respuesta.getBody());
    }

    @Test
    void solicitudInvalida_JsonMalFormado_Retorna400YMensajeGenerico() {
        // Arrange
        HttpMessageNotReadableException excepcion =
                mock(HttpMessageNotReadableException.class);

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody()
        );
    }

    @Test
    void solicitudInvalida_TipoDeParametroIncorrecto_Retorna400ConNombreParametro() {
        // Arrange
        MethodArgumentTypeMismatchException excepcion =
                mock(MethodArgumentTypeMismatchException.class);

        when(excepcion.getName()).thenReturn("id");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Parametro invalido: id", respuesta.getBody());
    }

    @Test
    void solicitudInvalida_MensajeNulo_UsaMensajeGenerico() {
        // Arrange
        IllegalArgumentException excepcion =
                new IllegalArgumentException((String) null);

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }

    @Test
    void solicitudInvalida_MensajeEnBlanco_UsaMensajeGenerico() {
        // Arrange
        IllegalArgumentException excepcion =
                new IllegalArgumentException(" ");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }

    // ERROR INESPERADO: HTTP 500

    @Test
    void errorInesperado_ExcepcionConMensaje_Retorna500YMensaje() {
        // Arrange
        Exception excepcion = new Exception("Fallo inesperado");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.errorInesperado(excepcion);

        // Assert
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );
        assertEquals("Ocurrio un error: Fallo inesperado", respuesta.getBody());
    }

    @Test
    void errorInesperado_MensajeEnBlanco_UsaMensajeGenerico() {
        // Arrange
        Exception excepcion = new Exception(" ");

        // Act
        ResponseEntity<String> respuesta =
                exceptionHandler.errorInesperado(excepcion);

        // Assert
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