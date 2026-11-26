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
        // Arrange
        DevolucionNoEncontradaException exception =
                new DevolucionNoEncontradaException("Devolucion no encontrada");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.noEncontrado(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Devolucion no encontrada", respuesta.getBody().mensaje());
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }

    @Test
    void conflicto_debeRetornar409YMensajeDeLaExcepcion() {
        // Arrange
        EjemplarNoDisponibleException exception =
                new EjemplarNoDisponibleException("El ejemplar no está disponible");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.conflicto(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El ejemplar no está disponible", respuesta.getBody().mensaje());
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }

    @Test
    void noAutorizado_debeRetornar403YMensajeDeLaExcepcion() {
        // Arrange
        UsuarioNoAutorizadoException exception =
                new UsuarioNoAutorizadoException("El usuario no está autorizado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.noAutorizado(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El usuario no está autorizado", respuesta.getBody().mensaje());
    }

    @Test
    void accesoDenegado_debeRetornar403YMensajeDeLaExcepcion() {
        // Arrange
        AccessDeniedException exception =
                new AccessDeniedException("Acceso denegado");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.accesoDenegado(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Acceso denegado", respuesta.getBody().mensaje());
    }

    @Test
    void servicioNoDisponible_debeRetornar503YMensajeDeLaExcepcion() {
        // Arrange
        ServicioExternoNoDisponibleException exception =
                new ServicioExternoNoDisponibleException("El servicio de catálogo no responde");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.servicioNoDisponible(exception);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El servicio de catálogo no responde", respuesta.getBody().mensaje());
    }

    @Test
    void validacion_debeRetornar400YDetallesDeCamposInvalidos() {
        // Arrange
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(
                        new FieldError("usuario", "correo", "El correo no es válido")
                )
        );

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.validacion(exception);

        // Assert
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
        // Arrange
        ConstraintViolationException exception =
                mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(exception.getConstraintViolations()).thenReturn(Set.of(violation));
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("idPrestamo");
        when(violation.getMessage()).thenReturn("El identificador es obligatorio");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.restriccion(exception);

        // Assert
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
        // Arrange
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.jsonInvalido(exception);

        // Assert
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
        // Arrange
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getName()).thenReturn("idPrestamo");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.parametroInvalido(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Parametro invalido: idPrestamo", respuesta.getBody().mensaje());
    }

    @Test
    void solicitudInvalida_debeRetornar400YMensajeDeLaExcepcion() {
        // Arrange
        IllegalArgumentException exception =
                new IllegalArgumentException("La fecha no es válida");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.solicitudInvalida(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("La fecha no es válida", respuesta.getBody().mensaje());
    }

    @Test
    void errorInesperado_debeRetornar500YMensajeGenerico() {
        // Arrange
        Exception exception = new Exception("Detalle interno del error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> respuesta =
                handler.errorInesperado(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody().mensaje()
        );
        assertEquals(Map.of(), respuesta.getBody().detalles());
    }
}