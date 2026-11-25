package com.biblioteca.notificacion.domain.exception;

public class DestinatarioInvalidoException extends RuntimeException {
    public DestinatarioInvalidoException(String message) {
        super(message);
    }

    public DestinatarioInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
