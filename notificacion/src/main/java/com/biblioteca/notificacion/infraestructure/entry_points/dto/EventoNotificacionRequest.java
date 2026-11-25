package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record EventoNotificacionRequest(
        @NotBlank(message = "El tipo de notificacion es obligatorio")
        String tipo,

        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        String referenciaId,

        @NotBlank(message = "El mensaje es obligatorio")
        String mensaje,

        LocalDateTime fecha
) {
}
