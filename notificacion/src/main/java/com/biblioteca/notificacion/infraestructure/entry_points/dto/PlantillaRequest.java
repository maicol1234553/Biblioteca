package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlantillaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 255, message = "El asunto no puede superar 255 caracteres")
        String asunto,

        @NotBlank(message = "El contenido es obligatorio")
        String contenido,

        String tipo,

        String estado
) {
}
