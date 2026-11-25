package com.biblioteca.notificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlantillaNotificacion {

    private String idPlantilla;
    private String nombre;
    private String asunto;
    private String contenido;
    private String tipo;
    private String estado;
}
