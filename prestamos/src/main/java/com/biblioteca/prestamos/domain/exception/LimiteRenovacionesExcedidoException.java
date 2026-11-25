package com.biblioteca.prestamos.domain.exception;

public class LimiteRenovacionesExcedidoException extends RuntimeException {
    public LimiteRenovacionesExcedidoException(String message) {
        super(message);
    }
}
