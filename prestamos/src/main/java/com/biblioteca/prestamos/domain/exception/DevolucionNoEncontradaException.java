package com.biblioteca.prestamos.domain.exception;

public class DevolucionNoEncontradaException extends RuntimeException {
    public DevolucionNoEncontradaException(String message) {
        super(message);
    }
}
