package com.biblioteca.prestamos.domain.exception;

public class RecursoEnUsoException extends RuntimeException {
    public RecursoEnUsoException(String message) {
        super(message);
    }
}
