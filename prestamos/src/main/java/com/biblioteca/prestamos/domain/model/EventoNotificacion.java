package com.biblioteca.prestamos.domain.model;

import java.time.LocalDateTime;

public record EventoNotificacion(
        TipoEventoNotificacion tipo,
        String idUsuario,
        String referenciaId,
        String mensaje,
        LocalDateTime fecha
) {
}
