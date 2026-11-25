package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ActualizarVencimientoRequest(
        @NotNull(message = "La nueva fecha de vencimiento es obligatoria")
        @Future(message = "La nueva fecha de vencimiento debe ser futura")
        LocalDateTime fechaVencimiento
) {
}
