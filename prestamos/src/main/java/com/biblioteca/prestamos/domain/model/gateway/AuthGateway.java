package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.UsuarioExterno;

public interface AuthGateway {

    /**
     * Devuelve null si el usuario no existe en Auth.
     * Lanza ServicioExternoNoDisponibleException si Auth no responde.
     */
    UsuarioExterno buscarUsuario(String idUsuario);
}
