package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import com.biblioteca.notificacion.domain.model.Recordatorio;

import java.time.LocalDateTime;

public record RecordatorioResponse(
        String idRecordatorio,
        String idUsuario,
        String idPrestamo,
        LocalDateTime fechaProgramada,
        String tipo,
        String estado
) {

    public static RecordatorioResponse from(Recordatorio recordatorio) {
        return new RecordatorioResponse(
                recordatorio.getIdRecordatorio(),
                recordatorio.getIdUsuario(),
                recordatorio.getIdPrestamo(),
                recordatorio.getFechaProgramada(),
                recordatorio.getTipo(),
                recordatorio.getEstado()
        );
    }
}
