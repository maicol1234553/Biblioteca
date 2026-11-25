package com.biblioteca.prestamos.domain.model;

public record EjemplarExterno(
        String idEjemplar,
        String idLibro,
        String tituloLibro,
        String codigoInventario,
        String ubicacion,
        String estado
) {

    public boolean estaDisponible() {
        return estado != null && estado.equalsIgnoreCase("DISPONIBLE");
    }
}
