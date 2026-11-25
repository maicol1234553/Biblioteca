package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PrestamoConfirmadoRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        @NotBlank(message = "El id del prestamo es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del prestamo debe ser un UUID valido")
        String idPrestamo,

        @Email(message = "El destinatario debe ser un correo valido")
        String destinatario,

        String mensaje
) {
}
