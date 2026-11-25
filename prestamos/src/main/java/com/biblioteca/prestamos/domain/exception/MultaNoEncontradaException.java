package com.biblioteca.prestamos.domain.exception;

public class MultaNoEncontradaException extends RuntimeException {
    public MultaNoEncontradaException(String message) {
        super(message);
    }
}
