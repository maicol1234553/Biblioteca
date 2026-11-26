package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.exception.EnvioSmsException;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.LimiteReintentosExcedidoException;
import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.PlantillaNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.RecordatorioDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioNoEncontradoException;
import com.biblioteca.notificacion.domain.exception.RecursoDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecursoEnUsoException;
import com.biblioteca.notificacion.domain.exception.ServicioExternoNoDisponibleException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(String mensaje, Map<String, String> detalles) {
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> rutaNoEncontrada(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Ruta no encontrada", Map.of()));
    }

    @ExceptionHandler({
            NotificacionNoEncontradaException.class,
            PlantillaNoEncontradaException.class,
            RecordatorioNoEncontradoException.class
    })
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException exception) {
        return respuesta(HttpStatus.NOT_FOUND, exception, Map.of());
    }

    @ExceptionHandler({
            RecordatorioDuplicadoException.class,
            RecursoDuplicadoException.class,
            LimiteReintentosExcedidoException.class,
            EstadoInvalidoException.class,
            RecursoEnUsoException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException exception) {
        return respuesta(HttpStatus.CONFLICT, exception, Map.of());
    }

    @ExceptionHandler(DestinatarioInvalidoException.class)
    public ResponseEntity<ErrorResponse> destinatarioInvalido(
            DestinatarioInvalidoException exception) {
        return respuesta(HttpStatus.BAD_REQUEST, exception, Map.of());
    }

    @ExceptionHandler(EnvioCorreoException.class)
    public ResponseEntity<ErrorResponse> errorCorreo(EnvioCorreoException exception) {
        log.warn("Error SMTP: {}", exception.getMessage());
        return respuesta(HttpStatus.BAD_GATEWAY, exception, Map.of());
    }

    @ExceptionHandler(EnvioSmsException.class)
    public ResponseEntity<ErrorResponse> errorSms(EnvioSmsException exception) {
        log.warn("Error SMS: {}", exception.getMessage());
        return respuesta(HttpStatus.BAD_GATEWAY, exception, Map.of());
    }

    @ExceptionHandler(ServicioExternoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> servicioNoDisponible(
            ServicioExternoNoDisponibleException exception) {
        log.warn("Servicio externo no disponible: {}", exception.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, exception, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException exception) {
        Map<String, String> detalles = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                detalles.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("La peticion tiene datos invalidos", detalles));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccion(ConstraintViolationException exception) {
        Map<String, String> detalles = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                detalles.put(violation.getPropertyPath().toString(), violation.getMessage()));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("La peticion tiene datos invalidos", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("El cuerpo de la peticion no es un JSON valido", Map.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(
            MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Parametro invalido: " + exception.getName(), Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> solicitudInvalida(IllegalArgumentException exception) {
        return respuesta(HttpStatus.BAD_REQUEST, exception, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception exception) {
        log.error("Error inesperado", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ocurrio un error procesando la solicitud", Map.of()));
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status,
                                                     Exception exception,
                                                     Map<String, String> detalles) {
        String mensaje = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Ocurrio un error procesando la solicitud"
                : exception.getMessage();

        return ResponseEntity.status(status)
                .body(new ErrorResponse(mensaje, detalles));
    }
}
