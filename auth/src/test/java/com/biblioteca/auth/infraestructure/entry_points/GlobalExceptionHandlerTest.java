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

        UsuarioPorIdNoEncontradoException exception =
                new UsuarioPorIdNoEncontradoException("Usuario no encontrado con id: 1");


        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Usuario no encontrado con id: 1", respuesta.getBody());
    }

    @Test
    void errorDeNegocio_cuandoUsuarioPorCorreoNoExiste_debeRetornarElMensaje() {

        UsuarioPorCorreoNoEncontradoException exception =
                new UsuarioPorCorreoNoEncontradoException(
                        "Usuario no encontrado con correo: usuario@correo.com"
                );


        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Usuario no encontrado con correo: usuario@correo.com",
                respuesta.getBody()
        );
    }

    @Test
    void errorDeNegocio_cuandoArgumentoEsInvalido_debeRetornarElMensaje() {

        IllegalArgumentException exception =
                new IllegalArgumentException("El correo no es válido");


        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("El correo no es válido", respuesta.getBody());
    }

    @Test
    void errorDeNegocio_cuandoMensajeEsBlanco_debeRetornarMensajeGenerico() {

        IllegalArgumentException exception = new IllegalArgumentException("   ");


        ResponseEntity<String> respuesta = handler.errorDeNegocio(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }

    @Test
    void jsonInvalido_debeRetornarMensajeDeJsonInvalido() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);


        ResponseEntity<String> respuesta = handler.jsonInvalido(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody()
        );
    }

    @Test
    void parametroInvalido_debeIncluirElNombreDelParametro() {

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("edad");


        ResponseEntity<String> respuesta = handler.parametroInvalido(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Parametro invalido: edad", respuesta.getBody());
    }

    @Test
    void errorInesperado_cuandoTieneMensaje_debeIncluirloEnLaRespuesta() {

        Exception exception = new Exception("Error de conexión");


        ResponseEntity<String> respuesta = handler.errorInesperado(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Ocurrio un error: Error de conexión", respuesta.getBody());
    }

    @Test
    void errorInesperado_cuandoMensajeEsNull_debeUsarMensajeGenerico() {

        Exception exception = new Exception((String) null);


        ResponseEntity<String> respuesta = handler.errorInesperado(exception);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error: Ocurrio un error procesando la solicitud",
                respuesta.getBody()
        );
    }
}