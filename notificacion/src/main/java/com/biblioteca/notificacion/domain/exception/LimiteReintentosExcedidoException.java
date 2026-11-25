package com.biblioteca.notificacion.domain.exception;

public class LimiteReintentosExcedidoException extends RuntimeException {
    public LimiteReintentosExcedidoException(String message) {
        super(message);
    }
}
