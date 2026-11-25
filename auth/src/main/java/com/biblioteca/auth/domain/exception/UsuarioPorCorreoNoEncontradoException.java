package com.biblioteca.auth.domain.exception;

public class UsuarioPorCorreoNoEncontradoException extends RuntimeException {
    public UsuarioPorCorreoNoEncontradoException(String message) {
        super(message);
    }
}
