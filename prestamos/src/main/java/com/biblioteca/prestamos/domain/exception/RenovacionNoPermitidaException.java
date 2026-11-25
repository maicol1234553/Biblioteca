package com.biblioteca.prestamos.domain.exception;

public class RenovacionNoPermitidaException extends RuntimeException {
    public RenovacionNoPermitidaException(String message) {
        super(message);
    }
}
