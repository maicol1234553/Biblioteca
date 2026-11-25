package com.biblioteca.prestamos.domain.exception;

public class LimitePrestamosExcedidoException extends RuntimeException {
    public LimitePrestamosExcedidoException(String message) {
        super(message);
    }
}
