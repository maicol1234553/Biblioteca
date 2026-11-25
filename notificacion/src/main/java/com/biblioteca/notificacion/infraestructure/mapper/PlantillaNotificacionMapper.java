package com.biblioteca.notificacion.infraestructure.mapper;

import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.infraestructure.driver_adapters.PlantillaNotificacionData;
import org.springframework.stereotype.Component;

@Component
public class PlantillaNotificacionMapper {

    public PlantillaNotificacion toPlantilla(PlantillaNotificacionData data) {
        if (data == null) {
            return null;
        }

        return new PlantillaNotificacion(
                data.getIdPlantilla(),
                data.getNombre(),
                data.getAsunto(),
                data.getContenido(),
                data.getTipo(),
                data.getEstado()
        );
    }

    public PlantillaNotificacionData toPlantillaData(PlantillaNotificacion plantilla) {
        return new PlantillaNotificacionData(
                plantilla.getIdPlantilla(),
                plantilla.getNombre(),
                plantilla.getAsunto(),
                plantilla.getContenido(),
                plantilla.getTipo(),
                plantilla.getEstado()
        );
    }
}
