package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.UsuarioContacto;

public interface AuthGateway {

    /**
     * Devuelve null si el usuario no existe en Auth.
     * Lanza ServicioExternoNoDisponibleException si Auth no responde.
     */
    UsuarioContacto buscarUsuario(String idUsuario);
}
