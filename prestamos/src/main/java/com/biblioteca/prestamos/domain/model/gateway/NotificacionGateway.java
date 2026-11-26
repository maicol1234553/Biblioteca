package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.EventoNotificacion;

public interface NotificacionGateway {





    void enviarEvento(EventoNotificacion evento);
}
