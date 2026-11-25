package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record RecordatorioDevolucionRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        @NotBlank(message = "El id del prestamo es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del prestamo debe ser un UUID valido")
        String idPrestamo,

        @Future(message = "La fecha programada debe ser futura")
        LocalDateTime fechaProgramada,

        String tipo
) {
}
