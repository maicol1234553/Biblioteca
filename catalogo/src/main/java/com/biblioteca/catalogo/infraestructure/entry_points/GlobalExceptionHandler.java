package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            AutorNoEncontradoException.class,
            EditorialNoEncontradaException.class,
            CategoriaNoEncontradaException.class,
            LibroNoEncontradoException.class,
            EjemplarNoEncontradoException.class
    })
    public ResponseEntity<String> noEncontrado(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mensajeDe(exception));
    }

    @ExceptionHandler({
            RecursoDuplicadoException.class,
            RecursoEnUsoException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<String> conflicto(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(mensajeDe(exception));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<String> solicitudInvalida(Exception exception) {
        if (exception instanceof HttpMessageNotReadableException) {
            return ResponseEntity.badRequest()
                    .body("El cuerpo de la peticion no es un JSON valido");
        }

        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return ResponseEntity.badRequest()
                    .body("Parametro invalido: " + mismatch.getName());
        }

        return ResponseEntity.badRequest().body(mensajeDe(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> errorInesperado(Exception exception) {
        log.error("Error inesperado", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrio un error: " + mensajeDe(exception));
    }

    private String mensajeDe(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Ocurrio un error procesando la solicitud"
                : exception.getMessage();
    }
}
