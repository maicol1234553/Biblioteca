package com.biblioteca.catalogo.domain.model;

import java.util.List;

public record DisponibilidadLibro(
        String idLibro,
        String titulo,
        int totalEjemplares,
        int disponibles,
        int prestados,
        int reservados,
        int enMantenimiento,
        int perdidos,
        boolean disponible,
        List<String> ubicacionesDisponibles
) {
}
