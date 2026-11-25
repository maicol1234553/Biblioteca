package com.biblioteca.auth.domain.model.gateway;

import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.Usuario;

public interface UsuarioGateway {
    Usuario guardarUsuario(Usuario usuario);

    Usuario buscarUsuarioPorId(String usuarioId);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario actualizarUsuario(Usuario usuario);

    void eliminarUsuario(String usuarioId);

    EstadisticasUsuario obtenerEstadisticas();
}
