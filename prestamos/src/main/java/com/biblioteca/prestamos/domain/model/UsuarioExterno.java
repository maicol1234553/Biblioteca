package com.biblioteca.prestamos.domain.model;

public record UsuarioExterno(
        String idUsuario,
        String nombre,
        String rol
) {

    public boolean esAdministrador() {
        return Rol.desde(rol) == Rol.ADMINISTRADOR;
    }
}
