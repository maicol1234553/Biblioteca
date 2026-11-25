package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReservarLibroRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario
) {
}
