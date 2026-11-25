package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.HistorialEnvio;

import java.util.List;

public interface HistorialEnvioGateway {

    HistorialEnvio guardarHistorial(HistorialEnvio historial);

    List<HistorialEnvio> listarPorNotificacion(String idNotificacion);

    long contarPorNotificacion(String idNotificacion);

    void eliminarPorNotificacion(String idNotificacion);
}
