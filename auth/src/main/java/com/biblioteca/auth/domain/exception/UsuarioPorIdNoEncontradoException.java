package com.biblioteca.auth.domain.exception;

public class UsuarioPorIdNoEncontradoException extends RuntimeException {
    public UsuarioPorIdNoEncontradoException(String message) {
        super(message);
    }
}
