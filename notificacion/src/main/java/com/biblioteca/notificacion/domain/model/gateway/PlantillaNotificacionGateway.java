package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;

import java.util.List;

public interface PlantillaNotificacionGateway {

    PlantillaNotificacion guardarPlantilla(PlantillaNotificacion plantilla);

    PlantillaNotificacion actualizarPlantilla(PlantillaNotificacion plantilla);

    PlantillaNotificacion buscarPlantillaPorId(String idPlantilla);

    PlantillaNotificacion buscarPlantillaPorNombre(String nombre);

    PlantillaNotificacion buscarActivaPorTipo(String tipo);

    List<PlantillaNotificacion> listarPlantillas();

    void eliminarPlantilla(String idPlantilla);
}
