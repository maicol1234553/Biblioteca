package com.biblioteca.notificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Recordatorio {

    private String idRecordatorio;
    private String idUsuario;
    private String idPrestamo;
    private LocalDateTime fechaProgramada;
    private String tipo;
    private String estado;
}
