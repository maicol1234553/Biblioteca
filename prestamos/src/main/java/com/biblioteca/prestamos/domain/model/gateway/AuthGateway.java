package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.UsuarioExterno;

public interface AuthGateway {





    UsuarioExterno buscarUsuario(String idUsuario);
}
