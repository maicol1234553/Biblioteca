package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.Notificacion;

import java.util.Collection;
import java.util.List;

public interface NotificacionGateway {

    Notificacion guardarNotificacion(Notificacion notificacion);

    Notificacion actualizarNotificacion(Notificacion notificacion);

    Notificacion buscarNotificacionPorId(String idNotificacion);

    List<Notificacion> listarNotificaciones();

    List<Notificacion> listarPorUsuario(String idUsuario);

    List<Notificacion> listarPorEstado(String estado);

    List<Notificacion> listarPorEstados(Collection<String> estados);

    void eliminarNotificacion(String idNotificacion);
}
