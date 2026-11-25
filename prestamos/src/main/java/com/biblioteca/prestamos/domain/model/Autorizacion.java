package com.biblioteca.prestamos.domain.model;

import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;

import java.util.Objects;

public final class Autorizacion {

    private Autorizacion() {
    }

    public static boolean esAdministrador(UsuarioExterno usuario) {
        if (usuario == null || usuario.rol() == null) {
            return false;
        }

        try {
            return Rol.desde(usuario.rol()) == Rol.ADMINISTRADOR;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean puedeAcceder(UsuarioExterno solicitante, String idUsuario) {
        return solicitante != null
                && (Objects.equals(solicitante.idUsuario(), idUsuario) || esAdministrador(solicitante));
    }

    public static void exigirAcceso(UsuarioExterno solicitante, String idUsuario) {
        if (!puedeAcceder(solicitante, idUsuario)) {
            throw new UsuarioNoAutorizadoException(
                    "No tiene permisos para operar sobre la informacion de otro usuario");
        }
    }

    public static void exigirAdministrador(UsuarioExterno solicitante) {
        if (!esAdministrador(solicitante)) {
            throw new UsuarioNoAutorizadoException(
                    "Esta operacion requiere el rol ADMINISTRADOR");
        }
    }
}
