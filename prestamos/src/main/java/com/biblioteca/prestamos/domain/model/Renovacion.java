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
public class Renovacion {

    private String idRenovacion;
    private String idPrestamo;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAnterior;
    private LocalDateTime fechaNueva;
    private String estado;
}
