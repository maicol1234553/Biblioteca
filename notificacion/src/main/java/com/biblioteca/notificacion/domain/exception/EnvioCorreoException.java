package com.biblioteca.notificacion.domain.exception;

public class EnvioCorreoException extends RuntimeException {
    public EnvioCorreoException(String message) {
        super(message);
    }

    public EnvioCorreoException(String message, Throwable cause) {
        super(message, cause);
    }
}
