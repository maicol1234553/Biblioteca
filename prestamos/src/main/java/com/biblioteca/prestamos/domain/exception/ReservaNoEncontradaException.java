package com.biblioteca.prestamos.domain.exception;

public class ReservaNoEncontradaException extends RuntimeException {
    public ReservaNoEncontradaException(String message) {
        super(message);
    }
}
