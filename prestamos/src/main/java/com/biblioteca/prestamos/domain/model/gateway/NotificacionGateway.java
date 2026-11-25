package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.EventoNotificacion;

public interface NotificacionGateway {

    /**
     * El envio de notificaciones no debe interrumpir el flujo principal:
     * el adaptador registra el error y continua.
     */
    void enviarEvento(EventoNotificacion evento);
}
