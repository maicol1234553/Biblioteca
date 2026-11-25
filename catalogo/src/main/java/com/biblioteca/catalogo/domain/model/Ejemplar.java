package com.biblioteca.catalogo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ejemplar {

    private String idEjemplar;
    private String idLibro;
    private String tituloLibro;
    private String codigoInventario;
    private String ubicacion;
    private String estado;
    private LocalDate fechaAdquisicion;
    private String observaciones;
}
