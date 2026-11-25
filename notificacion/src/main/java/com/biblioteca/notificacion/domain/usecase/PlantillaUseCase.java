package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.domain.exception.PlantillaNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.RecursoDuplicadoException;
import com.biblioteca.notificacion.domain.model.EstadoPlantilla;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.model.TipoNotificacion;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class PlantillaUseCase {

    private final PlantillaNotificacionGateway plantillaGateway;

    public PlantillaNotificacion crearPlantilla(PlantillaNotificacion plantilla) {
        validarPlantilla(plantilla);

        if (esBlanco(plantilla.getIdPlantilla())) {
            plantilla.setIdPlantilla(UUID.randomUUID().toString());
        }

        validarNombreDisponible(plantilla);
        normalizar(plantilla);

        return plantillaGateway.guardarPlantilla(plantilla);
    }

    public PlantillaNotificacion actualizarPlantilla(String idPlantilla,
                                                     PlantillaNotificacion plantilla) {
        buscarPlantillaInterna(idPlantilla);

        plantilla.setIdPlantilla(idPlantilla);
        validarPlantilla(plantilla);
        validarNombreDisponible(plantilla);
        normalizar(plantilla);

        return plantillaGateway.actualizarPlantilla(plantilla);
    }

    public PlantillaNotificacion buscarPlantilla(String idPlantilla) {
        return buscarPlantillaInterna(idPlantilla);
    }

    public List<PlantillaNotificacion> listarPlantillas() {
        return plantillaGateway.listarPlantillas();
    }

    public void eliminarPlantilla(String idPlantilla) {
        buscarPlantillaInterna(idPlantilla);
        plantillaGateway.eliminarPlantilla(idPlantilla);
    }

    private void validarPlantilla(PlantillaNotificacion plantilla) {
        if (plantilla == null) {
            throw new IllegalArgumentException("La plantilla es obligatoria");
        }

        if (esBlanco(plantilla.getNombre())) {
            throw new IllegalArgumentException("El nombre de la plantilla es obligatorio");
        }

        if (esBlanco(plantilla.getAsunto())) {
            throw new IllegalArgumentException("El asunto de la plantilla es obligatorio");
        }

        if (esBlanco(plantilla.getContenido())) {
            throw new IllegalArgumentException("El contenido de la plantilla es obligatorio");
        }
    }

    private void validarNombreDisponible(PlantillaNotificacion plantilla) {
        PlantillaNotificacion existente = plantillaGateway.buscarPlantillaPorNombre(
                plantilla.getNombre());

        if (existente != null
                && !Objects.equals(existente.getIdPlantilla(), plantilla.getIdPlantilla())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una plantilla con el nombre: " + plantilla.getNombre());
        }
    }

    private void normalizar(PlantillaNotificacion plantilla) {
        plantilla.setTipo(TipoNotificacion.desde(plantilla.getTipo()).name());
        plantilla.setEstado(EstadoPlantilla.desde(plantilla.getEstado()).name());
    }

    private PlantillaNotificacion buscarPlantillaInterna(String idPlantilla) {
        PlantillaNotificacion plantilla = plantillaGateway.buscarPlantillaPorId(idPlantilla);

        if (plantilla == null) {
            throw new PlantillaNoEncontradaException(
                    "Plantilla no encontrada con id: " + idPlantilla);
        }

        return plantilla;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
