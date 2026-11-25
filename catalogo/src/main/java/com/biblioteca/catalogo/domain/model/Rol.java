package com.biblioteca.catalogo.domain.model;

import java.util.Locale;

/**
 * Roles compartidos con el microservicio auth. El rol se valida contra la
 * tabla usuario de la base de datos comun, no contra un encabezado enviado
 * por el cliente.
 */
public enum Rol {
    ADMINISTRADOR,
    USUARIO;

    public static Rol desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return USUARIO;
        }

        String normalizado = valor.trim().toUpperCase(Locale.ROOT);

        if (normalizado.equals("ADMIN")) {
            return ADMINISTRADOR;
        }

        if (normalizado.equals("USER")) {
            return USUARIO;
        }

        try {
            return Rol.valueOf(normalizado);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El rol debe ser ADMINISTRADOR o USUARIO");
        }
    }
}
