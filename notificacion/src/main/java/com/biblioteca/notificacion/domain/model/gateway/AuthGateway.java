package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.UsuarioContacto;

public interface AuthGateway {





    UsuarioContacto buscarUsuario(String idUsuario);
}
