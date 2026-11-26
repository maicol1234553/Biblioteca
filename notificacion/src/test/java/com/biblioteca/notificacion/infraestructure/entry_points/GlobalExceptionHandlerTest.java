package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.LimiteReintentosExcedidoException;
import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.PlantillaNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.RecordatorioDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioNoEncontradoException;
import com.biblioteca.notificacion.domain.exception.RecursoDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecursoEnUsoException;
import com.biblioteca.notificacion.domain.exception.ServicioExternoNoDisponibleException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // RUTA NO ENCONTRADA: HTTP 404

    @Test
    void rutaNoEncontrada_RecursoNoEncontrado_Retorna404() {
        // Arrange
        NoResourceFoundException excepcion =
                mock(NoResourceFoundException.class);

        // Act
        var respuesta = exceptionHandler.rutaNoEncontrada(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ruta no encontrada", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    @Test
    void rutaNoEncontrada_SinHandler_Retorna404() {
        // Arrange
        NoHandlerFoundException excepcion =
                mock(NoHandlerFoundException.class);

        // Act
        var respuesta = exceptionHandler.rutaNoEncontrada(excepcion);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ruta no encontrada", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // RECURSOS NO ENCONTRADOS: HTTP 404

    @Test
    void noEncontrado_ExcepcionesDeDominio_Retorna404YConservaMensaje() {
        // Arrange
        List<RuntimeException> excepciones = List.of(
                new NotificacionNoEncontradaException("Notificación no encontrada"),
                new PlantillaNoEncontradaException("Plantilla no encontrada"),
                new RecordatorioNoEncontradoException("Recordatorio no encontrado")
        );

        // Act y Assert
        for (RuntimeException excepcion : excepciones) {
            var respuesta = exceptionHandler.noEncontrado(excepcion);

            assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
            assertEquals(excepcion.getMessage(), respuesta.getBody().mensaje());
            assertTrue(respuesta.getBody().detalles().isEmpty());
        }
    }

    // CONFLICTOS: HTTP 409

    @Test
    void conflicto_ExcepcionesDeConflicto_Retorna409YConservaMensaje() {
        // Arrange
        List<RuntimeException> excepciones = List.of(
                new RecordatorioDuplicadoException("Recordatorio duplicado"),
                new RecursoDuplicadoException("Recurso duplicado"),
                new LimiteReintentosExcedidoException("Límite de reintentos excedido"),
                new EstadoInvalidoException("Estado inválido"),
                new RecursoEnUsoException("Recurso en uso"),
                new DataIntegrityViolationException("Conflicto de integridad")
        );

        // Act y Assert
        for (RuntimeException excepcion : excepciones) {
            var respuesta = exceptionHandler.conflicto(excepcion);

            assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
            assertEquals(excepcion.getMessage(), respuesta.getBody().mensaje());
            assertTrue(respuesta.getBody().detalles().isEmpty());
        }
    }

    // DESTINATARIO INVÁLIDO: HTTP 400

    @Test
    void destinatarioInvalido_Retorna400YMensaje() {
        // Arrange
        DestinatarioInvalidoException excepcion =
                new DestinatarioInvalidoException("Destinatario inválido");

        // Act
        var respuesta = exceptionHandler.destinatarioInvalido(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Destinatario inválido", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // ERROR DE CORREO: HTTP 502

    @Test
    void errorCorreo_Retorna502YMensaje() {
        // Arrange
        EnvioCorreoException excepcion =
                new EnvioCorreoException("No se pudo enviar el correo");

        // Act
        var respuesta = exceptionHandler.errorCorreo(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, respuesta.getStatusCode());
        assertEquals(
                "No se pudo enviar el correo",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // SERVICIO EXTERNO NO DISPONIBLE: HTTP 503

    @Test
    void servicioNoDisponible_Retorna503YMensaje() {
        // Arrange
        ServicioExternoNoDisponibleException excepcion =
                new ServicioExternoNoDisponibleException(
                        "El proveedor externo no está disponible"
                );

        // Act
        var respuesta =
                exceptionHandler.servicioNoDisponible(excepcion);

        // Assert
        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                respuesta.getStatusCode()
        );
        assertEquals(
                "El proveedor externo no está disponible",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // VALIDACIÓN DE CAMPOS: HTTP 400

    @Test
    void validacion_ErroresDeCampos_Retorna400ConDetalles() {
        // Arrange
        MethodArgumentNotValidException excepcion =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError errorCorreo = new FieldError(
                "request",
                "correo",
                "El correo es obligatorio"
        );

        when(excepcion.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(errorCorreo));

        // Act
        var respuesta = exceptionHandler.validacion(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "La peticion tiene datos invalidos",
                respuesta.getBody().mensaje()
        );
        assertEquals(
                Map.of("correo", "El correo es obligatorio"),
                respuesta.getBody().detalles()
        );
    }

    // RESTRICCIONES: HTTP 400

    @Test
    void restriccion_ConstraintViolation_Retorna400ConDetalles() {
        // Arrange
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("enviar.correo");
        when(violation.getMessage()).thenReturn("El formato no es válido");

        Set<ConstraintViolation<?>> violaciones =
                new LinkedHashSet<>(Set.of(violation));

        ConstraintViolationException excepcion =
                new ConstraintViolationException("Validación fallida", violaciones);

        // Act
        var respuesta = exceptionHandler.restriccion(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "La peticion tiene datos invalidos",
                respuesta.getBody().mensaje()
        );
        assertEquals(
                Map.of("enviar.correo", "El formato no es válido"),
                respuesta.getBody().detalles()
        );
    }

    // JSON INVÁLIDO: HTTP 400

    @Test
    void jsonInvalido_Retorna400YMensajeGenerico() {
        // Arrange
        HttpMessageNotReadableException excepcion =
                mock(HttpMessageNotReadableException.class);

        // Act
        var respuesta = exceptionHandler.jsonInvalido(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // TIPO DE PARÁMETRO INCORRECTO: HTTP 400

    @Test
    void parametroInvalido_Retorna400IncluyendoNombreDelParametro() {
        // Arrange
        MethodArgumentTypeMismatchException excepcion =
                mock(MethodArgumentTypeMismatchException.class);

        when(excepcion.getName()).thenReturn("id");

        // Act
        var respuesta = exceptionHandler.parametroInvalido(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Parametro invalido: id",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    // ARGUMENTO INVÁLIDO: HTTP 400

    @Test
    void solicitudInvalida_ConMensaje_Retorna400YConservaMensaje() {
        // Arrange
        IllegalArgumentException excepcion =
                new IllegalArgumentException("El usuario es obligatorio");

        // Act
        var respuesta = exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El usuario es obligatorio",
                respuesta.getBody().mensaje()
        );
    }

    @Test
    void solicitudInvalida_MensajeEnBlanco_UsaMensajeGenerico() {
        // Arrange
        IllegalArgumentException excepcion =
                new IllegalArgumentException(" ");

        // Act
        var respuesta = exceptionHandler.solicitudInvalida(excepcion);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody().mensaje()
        );
    }

    // ERROR INESPERADO: HTTP 500

    @Test
    void errorInesperado_Retorna500YMensajeGenerico() {
        // Arrange
        Exception excepcion = new Exception("Error interno");

        // Act
        var respuesta = exceptionHandler.errorInesperado(excepcion);

        // Assert
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                respuesta.getStatusCode()
        );
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }
}