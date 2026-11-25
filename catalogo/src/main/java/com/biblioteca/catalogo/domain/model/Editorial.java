package com.biblioteca.catalogo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Editorial {

    private String idEditorial;
    private String nombre;
    private String pais;
    private String sitioWeb;
    private String correoContacto;
}
