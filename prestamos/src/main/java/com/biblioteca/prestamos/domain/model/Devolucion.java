package com.biblioteca.prestamos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Devolucion {

    private String idDevolucion;
    private String idPrestamo;
    private LocalDateTime fechaDevolucion;
    private String observaciones;
    private String estadoEjemplar;
}
