package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.infraestructure.driver_adapters.NotificacionData;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

    public Notificacion toNotificacion(NotificacionData data) {
        if (data == null) {
            return null;
        }

        return new Notificacion(
                data.getIdNotificacion(),
                data.getIdUsuario(),
                data.getDestinatario(),
                data.getAsunto(),
                data.getMensaje(),
                data.getTipo(),
                data.getFechaCreacion(),
                data.getFechaEnvio(),
                data.getEstado()
        );
    }

    public NotificacionData toNotificacionData(Notificacion notificacion) {
        return new NotificacionData(
                notificacion.getIdNotificacion(),
                notificacion.getIdUsuario(),
                notificacion.getDestinatario(),
                notificacion.getAsunto(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaEnvio(),
                notificacion.getEstado()
        );
    }
}
