package com.biblioteca.prestamos.domain.exception;

public class PrestamoYaDevueltoException extends RuntimeException {
    public PrestamoYaDevueltoException(String message) {
        super(message);
    }
}
