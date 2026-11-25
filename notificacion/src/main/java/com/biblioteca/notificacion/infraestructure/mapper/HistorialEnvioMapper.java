package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.infraestructure.driver_adapters.HistorialEnvioData;
import com.biblioteca.notificacion.infraestructure.driver_adapters.NotificacionData;
import org.springframework.stereotype.Component;

@Component
public class HistorialEnvioMapper {

    public HistorialEnvio toHistorial(HistorialEnvioData data) {
        if (data == null) {
            return null;
        }

        return new HistorialEnvio(
                data.getIdHistorial(),
                data.getNotificacion() == null
                        ? null
                        : data.getNotificacion().getIdNotificacion(),
                data.getFechaIntento(),
                data.getResultado(),
                data.getMensajeError()
        );
    }

    public HistorialEnvioData toHistorialData(HistorialEnvio historial,
                                             NotificacionData notificacion) {
        return new HistorialEnvioData(
                historial.getIdHistorial(),
                notificacion,
                historial.getFechaIntento(),
                historial.getResultado(),
                historial.getMensajeError()
        );
    }
}
