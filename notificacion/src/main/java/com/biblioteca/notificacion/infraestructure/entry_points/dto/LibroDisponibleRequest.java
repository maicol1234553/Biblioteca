package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LibroDisponibleRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        @NotBlank(message = "El id del libro es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del libro debe ser un UUID valido")
        String idLibro,

        @Email(message = "El destinatario debe ser un correo valido")
        String destinatario,

        String mensaje
) {
}
