package com.biblioteca.prestamos.domain.model;

public record DisponibilidadLibroExterna(
        String idLibro,
        String titulo,
        int totalEjemplares,
        int disponibles,
        boolean disponible
) {
}
