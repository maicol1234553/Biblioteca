package com.biblioteca.notificacion.domain.model;

import java.time.LocalDateTime;

public record EventoNotificacion(
        String tipo,
        String idUsuario,
        String referenciaId,
        String mensaje,
        LocalDateTime fecha
) {
}
