package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DevolverPrestamoRequest(
        @NotBlank(message = "La condicion del ejemplar es obligatoria")
        @Pattern(
                regexp = "(?iu)BUENO|DANADO|DAÑADO|PERDIDO|BUEN_ESTADO",
                message = "La condicion debe ser BUENO, DANADO o PERDIDO"
        )
        String estadoEjemplar,

        @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
        String observaciones
) {
}
