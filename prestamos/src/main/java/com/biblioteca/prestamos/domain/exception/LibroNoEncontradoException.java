package com.biblioteca.prestamos.domain.exception;

public class LibroNoEncontradoException extends RuntimeException {
    public LibroNoEncontradoException(String message) {
        super(message);
    }
}
