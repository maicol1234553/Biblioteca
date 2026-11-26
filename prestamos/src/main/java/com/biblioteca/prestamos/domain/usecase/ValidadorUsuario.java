package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoEncontradoException;
import com.biblioteca.prestamos.domain.model.Rol;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;

public final class ValidadorUsuario {

    private ValidadorUsuario() {
    }






    public static UsuarioExterno validar(AuthGateway authGateway,
                                         UsuarioExterno solicitante,
                                         String idUsuario) {
        UsuarioExterno usuario = solicitante != null && solicitante.idUsuario() != null
                && solicitante.idUsuario().equals(idUsuario)
                ? solicitante
                : authGateway.buscarUsuario(idUsuario);

        if (usuario == null) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado con id: " + idUsuario);
        }

        if (usuario.idUsuario() == null || !usuario.idUsuario().equals(idUsuario)) {
            throw new UsuarioNoAutorizadoException("El usuario consultado no coincide con la respuesta de Auth");
        }

        Rol rol;

        try {
            rol = Rol.desde(usuario.rol());
        } catch (IllegalArgumentException exception) {
            throw new UsuarioNoAutorizadoException("El usuario no tiene un rol autorizado");
        }

        return new UsuarioExterno(usuario.idUsuario(), usuario.nombre(), rol.name());
    }
}
