package com.biblioteca.prestamos.domain.model.gateway;

import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;

public interface CatalogoGateway {





    EjemplarExterno buscarEjemplar(String idEjemplar);





    DisponibilidadLibroExterna consultarDisponibilidadLibro(String idLibro);

    void actualizarEstadoEjemplar(String idEjemplar, String estado);
}
