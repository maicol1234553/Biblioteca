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



    @Test
    void rutaNoEncontrada_RecursoNoEncontrado_Retorna404() {

        NoResourceFoundException excepcion =
                mock(NoResourceFoundException.class);


        var respuesta = exceptionHandler.rutaNoEncontrada(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ruta no encontrada", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }

    @Test
    void rutaNoEncontrada_SinHandler_Retorna404() {

        NoHandlerFoundException excepcion =
                mock(NoHandlerFoundException.class);


        var respuesta = exceptionHandler.rutaNoEncontrada(excepcion);


        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Ruta no encontrada", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }



    @Test
    void noEncontrado_ExcepcionesDeDominio_Retorna404YConservaMensaje() {

        List<RuntimeException> excepciones = List.of(
                new NotificacionNoEncontradaException("Notificación no encontrada"),
                new PlantillaNoEncontradaException("Plantilla no encontrada"),
                new RecordatorioNoEncontradoException("Recordatorio no encontrado")
        );


        for (RuntimeException excepcion : excepciones) {
            var respuesta = exceptionHandler.noEncontrado(excepcion);

            assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
            assertEquals(excepcion.getMessage(), respuesta.getBody().mensaje());
            assertTrue(respuesta.getBody().detalles().isEmpty());
        }
    }



    @Test
    void conflicto_ExcepcionesDeConflicto_Retorna409YConservaMensaje() {

        List<RuntimeException> excepciones = List.of(
                new RecordatorioDuplicadoException("Recordatorio duplicado"),
                new RecursoDuplicadoException("Recurso duplicado"),
                new LimiteReintentosExcedidoException("Límite de reintentos excedido"),
                new EstadoInvalidoException("Estado inválido"),
                new RecursoEnUsoException("Recurso en uso"),
                new DataIntegrityViolationException("Conflicto de integridad")
        );


        for (RuntimeException excepcion : excepciones) {
            var respuesta = exceptionHandler.conflicto(excepcion);

            assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
            assertEquals(excepcion.getMessage(), respuesta.getBody().mensaje());
            assertTrue(respuesta.getBody().detalles().isEmpty());
        }
    }



    @Test
    void destinatarioInvalido_Retorna400YMensaje() {

        DestinatarioInvalidoException excepcion =
                new DestinatarioInvalidoException("Destinatario inválido");


        var respuesta = exceptionHandler.destinatarioInvalido(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Destinatario inválido", respuesta.getBody().mensaje());
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }



    @Test
    void errorCorreo_Retorna502YMensaje() {

        EnvioCorreoException excepcion =
                new EnvioCorreoException("No se pudo enviar el correo");


        var respuesta = exceptionHandler.errorCorreo(excepcion);


        assertEquals(HttpStatus.BAD_GATEWAY, respuesta.getStatusCode());
        assertEquals(
                "No se pudo enviar el correo",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }



    @Test
    void servicioNoDisponible_Retorna503YMensaje() {

        ServicioExternoNoDisponibleException excepcion =
                new ServicioExternoNoDisponibleException(
                        "El proveedor externo no está disponible"
                );


        var respuesta =
                exceptionHandler.servicioNoDisponible(excepcion);


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



    @Test
    void validacion_ErroresDeCampos_Retorna400ConDetalles() {

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


        var respuesta = exceptionHandler.validacion(excepcion);


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



    @Test
    void restriccion_ConstraintViolation_Retorna400ConDetalles() {

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("enviar.correo");
        when(violation.getMessage()).thenReturn("El formato no es válido");

        Set<ConstraintViolation<?>> violaciones =
                new LinkedHashSet<>(Set.of(violation));

        ConstraintViolationException excepcion =
                new ConstraintViolationException("Validación fallida", violaciones);


        var respuesta = exceptionHandler.restriccion(excepcion);


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



    @Test
    void jsonInvalido_Retorna400YMensajeGenerico() {

        HttpMessageNotReadableException excepcion =
                mock(HttpMessageNotReadableException.class);


        var respuesta = exceptionHandler.jsonInvalido(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El cuerpo de la peticion no es un JSON valido",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }



    @Test
    void parametroInvalido_Retorna400IncluyendoNombreDelParametro() {

        MethodArgumentTypeMismatchException excepcion =
                mock(MethodArgumentTypeMismatchException.class);

        when(excepcion.getName()).thenReturn("id");


        var respuesta = exceptionHandler.parametroInvalido(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Parametro invalido: id",
                respuesta.getBody().mensaje()
        );
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }



    @Test
    void solicitudInvalida_ConMensaje_Retorna400YConservaMensaje() {

        IllegalArgumentException excepcion =
                new IllegalArgumentException("El usuario es obligatorio");


        var respuesta = exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "El usuario es obligatorio",
                respuesta.getBody().mensaje()
        );
    }

    @Test
    void solicitudInvalida_MensajeEnBlanco_UsaMensajeGenerico() {

        IllegalArgumentException excepcion =
                new IllegalArgumentException(" ");


        var respuesta = exceptionHandler.solicitudInvalida(excepcion);


        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
                "Ocurrio un error procesando la solicitud",
                respuesta.getBody().mensaje()
        );
    }



    @Test
    void errorInesperado_Retorna500YMensajeGenerico() {

        Exception excepcion = new Exception("Error interno");


        var respuesta = exceptionHandler.errorInesperado(excepcion);


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