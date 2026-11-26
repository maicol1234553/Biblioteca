package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnviarSmsRequest(
        @NotBlank(message = "El id del usuario es obligatorio")
        @Pattern(regexp = Validacion.UUID_REGEX, message = "El id del usuario debe ser un UUID valido")
        String idUsuario,

        /**
         * Telefono opcional: 10 digitos colombianos (3001234567), E.164
         * (+573001234567) o vacio para tomarlo del usuario en auth.
         */
        @Size(max = 25, message = "El telefono no puede superar 25 caracteres")
        String destinatario,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        String mensaje,

        String tipo
) {
}
