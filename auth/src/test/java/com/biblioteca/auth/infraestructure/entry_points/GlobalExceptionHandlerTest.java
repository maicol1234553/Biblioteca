package com.biblioteca.auth.infraestructure.entry_points;

import com.biblioteca.auth.domain.exception.UsuarioPorCorreoNoEncontradoException;
import com.biblioteca.auth.domain.exception.UsuarioPorIdNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void errorDeNegocio_cuandoUsuarioPorIdNoExiste_debeRetornarElMensaje() {
        // Arrange
        UsuarioPorIdNoEncontradoException exception =
                new UsuarioPorIdNoEncontradoException("Usuario no encontrado con id: 1");

        // Act
        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Usuario no encontrado con id: 1", respuesta.getBody());
    }

    @Test
    void errorDeNegocio_cuandoUsuarioPorCorreoNoExiste_debeRetornarElMensaje() {
        // Arrange
        UsuarioPorCorreoNoEncontradoException exception =
                new UsuarioPorCorreoNoEncontradoException(
                        "Usuario no encontrado con correo: usuario@correo.com"
                );

        // Act
        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Usuario no encontrado con correo: usuario@correo.com",
                respuesta.getBody()
        );
    }

    @Test
    void errorDeNegocio_cuandoArgumentoEsInvalido_debeRetornarElMensaje() {
        // Arrange
        IllegalArgumentException exception =
                new IllegalArgumentException("El correo no es válido");

        // Act
        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("El correo no es válido", respuesta.getBody());
    }

    @Test
    void errorDeNegocio_cuandoMensajeEsBlanco_debeRetornarMensajeGenerico() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("   ");

        // Act
        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }

    @Test
    void jsonInvalido_debeRetornarMensajeDeJsonInvalido() {
        // Arrange
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        // Act
        ResponseEntity<String> respuesta = handler.jsonInvalido(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody()
        );
    }

    @Test
    void parametroInvalido_debeIncluirElNombreDelParametro() {
        // Arrange
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("edad");

        // Act
        ResponseEntity<String> respuesta = handler.parametroInvalido(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Parametro invalido: edad", respuesta.getBody());
    }

    @Test
    void errorInesperado_cuandoTieneMensaje_debeIncluirloEnLaRespuesta() {
        // Arrange
        Exception exception = new Exception("Error de conexión");

        // Act
        ResponseEntity<String> respuesta = handler.errorInesperado(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Ocurrio un error: Error de conexión", respuesta.getBody());
    }

    @Test
    void errorInesperado_cuandoMensajeEsNull_debeUsarMensajeGenerico() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ResponseEntity<String> respuesta = handler.errorInesperado(exception);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error: Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }
}