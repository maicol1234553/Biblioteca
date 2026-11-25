package com.biblioteca.prestamos.infraestructure.entry_points.dto;

public final class Validacion {

    public static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private Validacion() {
    }
}
