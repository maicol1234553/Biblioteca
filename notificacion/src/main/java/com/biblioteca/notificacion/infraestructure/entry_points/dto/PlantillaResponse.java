package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;

public record PlantillaResponse(
        String idPlantilla,
        String nombre,
        String asunto,
        String contenido,
        String tipo,
        String estado
) {

    public static PlantillaResponse from(PlantillaNotificacion plantilla) {
        return new PlantillaResponse(
                plantilla.getIdPlantilla(),
                plantilla.getNombre(),
                plantilla.getAsunto(),
                plantilla.getContenido(),
                plantilla.getTipo(),
                plantilla.getEstado()
        );
    }
}
