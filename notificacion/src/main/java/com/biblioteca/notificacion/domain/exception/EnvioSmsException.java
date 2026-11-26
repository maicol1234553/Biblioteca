package com.biblioteca.notificacion.domain.exception;

public class EnvioSmsException extends RuntimeException {
    public EnvioSmsException(String message) {
        super(message);
    }

    public EnvioSmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
