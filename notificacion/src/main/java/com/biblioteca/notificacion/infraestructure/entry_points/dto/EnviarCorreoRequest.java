package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnviarCorreoRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        @Email(message = "El destinatario debe ser un correo valido")
        @Size(max = 320, message = "El destinatario no puede superar 320 caracteres")
        String destinatario,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 255, message = "El asunto no puede superar 255 caracteres")
        String asunto,

        @NotBlank(message = "El mensaje es obligatorio")
        String mensaje,

        String tipo
) {
}
