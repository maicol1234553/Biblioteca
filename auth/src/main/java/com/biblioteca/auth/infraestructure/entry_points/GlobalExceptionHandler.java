package com.biblioteca.auth.infraestructure.entry_points;

import com.biblioteca.auth.domain.exception.UsuarioPorCorreoNoEncontradoException;
import com.biblioteca.auth.domain.exception.UsuarioPorIdNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            UsuarioPorIdNoEncontradoException.class,
            UsuarioPorCorreoNoEncontradoException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<String> errorDeNegocio(RuntimeException exception) {
        return ResponseEntity.ok(mensajeDe(exception));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> jsonInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity.ok("El cuerpo de la peticion no es un JSON valido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> parametroInvalido(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.ok("Parametro invalido: " + exception.getName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> errorInesperado(Exception exception) {
        log.error("Error inesperado", exception);
        return ResponseEntity.ok("Ocurrio un error: " + mensajeDe(exception));
    }

    private String mensajeDe(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Ocurrio un error procesando la solicitud"
                : exception.getMessage();
    }
}
