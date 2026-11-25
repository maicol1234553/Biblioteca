package com.biblioteca.prestamos.domain.exception;

public class ReservaDuplicadaException extends RuntimeException {
    public ReservaDuplicadaException(String message) {
        super(message);
    }
}
