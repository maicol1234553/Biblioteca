package com.biblioteca.prestamos.domain.exception;

public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String message) {
        super(message);
    }
}
