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
public class HistorialEnvio {

    private String idHistorial;
    private String idNotificacion;
    private LocalDateTime fechaIntento;
    private String resultado;
    private String mensajeError;
}
