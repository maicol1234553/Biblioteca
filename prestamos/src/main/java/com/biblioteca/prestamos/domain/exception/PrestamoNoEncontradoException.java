package com.biblioteca.prestamos.domain.exception;

public class PrestamoNoEncontradoException extends RuntimeException {
    public PrestamoNoEncontradoException(String message) {
        super(message);
    }
}
