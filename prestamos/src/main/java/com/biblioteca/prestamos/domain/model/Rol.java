package com.biblioteca.prestamos.domain.model;

import java.util.Locale;

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
