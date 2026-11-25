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
public class Reserva {

    private String idReserva;
    private String idUsuario;
    private String idLibro;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaExpiracion;
    private String estado;
}
