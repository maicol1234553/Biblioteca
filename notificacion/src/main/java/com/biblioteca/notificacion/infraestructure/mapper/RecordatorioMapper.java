package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.infraestructure.driver_adapters.RecordatorioData;
import org.springframework.stereotype.Component;

@Component
public class RecordatorioMapper {

    public Recordatorio toRecordatorio(RecordatorioData data) {
        if (data == null) {
            return null;
        }

        return new Recordatorio(
                data.getIdRecordatorio(),
                data.getIdUsuario(),
                data.getIdPrestamo(),
                data.getFechaProgramada(),
                data.getTipo(),
                data.getEstado()
        );
    }

    public RecordatorioData toRecordatorioData(Recordatorio recordatorio) {
        return new RecordatorioData(
                recordatorio.getIdRecordatorio(),
                recordatorio.getIdUsuario(),
                recordatorio.getIdPrestamo(),
                recordatorio.getFechaProgramada(),
                recordatorio.getTipo(),
                recordatorio.getEstado()
        );
    }
}
