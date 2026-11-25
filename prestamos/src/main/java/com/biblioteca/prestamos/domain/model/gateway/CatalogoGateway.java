package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;

public interface CatalogoGateway {

    /**
     * Devuelve null si el ejemplar no existe en Catalogo.
     * Lanza ServicioExternoNoDisponibleException si Catalogo no responde.
     */
    EjemplarExterno buscarEjemplar(String idEjemplar);

    /**
     * Devuelve null si el libro no existe en Catalogo.
     * Lanza ServicioExternoNoDisponibleException si Catalogo no responde.
     */
    DisponibilidadLibroExterna consultarDisponibilidadLibro(String idLibro);

    void actualizarEstadoEjemplar(String idEjemplar, String estado);
}
