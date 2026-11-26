package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.exception.DevolucionNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.EjemplarNoDisponibleException;
import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void noEncontrado_debeRetornar404YMensajeDeLaExcepcion() {

        DevolucionNoEncontradaException exception =
                new DevolucionNoEncontradaException("Devolucion no encontrada");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.noEncontrado(exception);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Devolucion no encontrada", respuesta.getBody().mensaje());
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }

    @Test
    void conflicto_debeRetornar409YMensajeDeLaExcepcion() {

        EjemplarNoDisponibleException exception =
                new EjemplarNoDisponibleException("El ejemplar no está disponible");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.conflicto(exception);


        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El ejemplar no está disponible", respuesta.getBody().mensaje());
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }

    @Test
    void noAutorizado_debeRetornar403YMensajeDeLaExcepcion() {

        UsuarioNoAutorizadoException exception =
                new UsuarioNoAutorizadoException("El usuario no está autorizado");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.noAutorizado(exception);


        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El usuario no está autorizado", respuesta.getBody().mensaje());
    }

    @Test
    void accesoDenegado_debeRetornar403YMensajeDeLaExcepcion() {

        AccessDeniedException exception =
                new AccessDeniedException("Acceso denegado");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.accesoDenegado(exception);


        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Acceso denegado", respuesta.getBody().mensaje());
    }

    @Test
    void servicioNoDisponible_debeRetornar503YMensajeDeLaExcepcion() {

        ServicioExternoNoDisponibleException exception =
                new ServicioExternoNoDisponibleException("El servicio de catálogo no responde");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.servicioNoDisponible(exception);


        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El servicio de catálogo no responde", respuesta.getBody().mensaje());
    }

    @Test
    void validacion_debeRetornar400YDetallesDeCamposInvalidos() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(
                        new FieldError("usuario", "correo", "El correo no es válido")
                )
        );


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.validacion(exception);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("La peticion tiene datos invalidos", respuesta.getBody().mensaje());
        assertEquals(
                "El correo no es válido",
                respuesta.getBody().detalles().get("correo")
        );
    }

    @Test
    void restriccion_debeRetornar400YDetalleDeLaRestriccion() {

        ConstraintViolationException exception =
                mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(exception.getConstraintViolations()).thenReturn(Set.of(violation));
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("idPrestamo");
        when(violation.getMessage()).thenReturn("El identificador es obligatorio");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.restriccion(exception);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("La peticion tiene datos invalidos", respuesta.getBody().mensaje());
        assertEquals(
                "El identificador es obligatorio",
                respuesta.getBody().detalles().get("idPrestamo")
        );
    }

    @Test
    void jsonInvalido_debeRetornar400YMensajeDeJsonInvalido() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.jsonInvalido(exception);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody().mensaje()
        );
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }

    @Test
    void parametroInvalido_debeRetornar400YNombreDelParametro() {

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("idPrestamo");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.parametroInvalido(exception);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Parametro invalido: idPrestamo", respuesta.getBody().mensaje());
    }

    @Test
    void solicitudInvalida_debeRetornar400YMensajeDeLaExcepcion() {

        IllegalArgumentException exception =
                new IllegalArgumentException("La fecha no es válida");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.solicitudInvalida(exception);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("La fecha no es válida", respuesta.getBody().mensaje());
    }

    @Test
    void errorInesperado_debeRetornar500YMensajeGenerico() {

        Exception exception = new Exception("Detalle interno del error");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.errorInesperado(exception);


        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody().mensaje()
        );
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }
}