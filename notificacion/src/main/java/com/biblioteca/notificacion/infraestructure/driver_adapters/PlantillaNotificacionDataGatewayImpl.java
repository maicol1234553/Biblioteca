package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.model.EstadoPlantilla;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import com.biblioteca.notificacion.infraestructure.mapper.PlantillaNotificacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlantillaNotificacionDataGatewayImpl implements PlantillaNotificacionGateway {

    private final PlantillaNotificacionDataJpaRepository plantillaDataJpaRepository;
    private final PlantillaNotificacionMapper plantillaMapper;

    @Override
    @Transactional
    public PlantillaNotificacion guardarPlantilla(PlantillaNotificacion plantilla) {
        return plantillaMapper.toPlantilla(
                plantillaDataJpaRepository.save(plantillaMapper.toPlantillaData(plantilla)));
    }

    @Override
    @Transactional
    public PlantillaNotificacion actualizarPlantilla(PlantillaNotificacion plantilla) {
        return plantillaMapper.toPlantilla(
                plantillaDataJpaRepository.save(plantillaMapper.toPlantillaData(plantilla)));
    }

    @Override
    public PlantillaNotificacion buscarPlantillaPorId(String idPlantilla) {
        return plantillaDataJpaRepository.findById(idPlantilla)
                .map(plantillaMapper::toPlantilla)
                .orElse(null);
    }

    @Override
    public PlantillaNotificacion buscarPlantillaPorNombre(String nombre) {
        return plantillaDataJpaRepository.findFirstByNombreIgnoreCase(nombre)
                .map(plantillaMapper::toPlantilla)
                .orElse(null);
    }

    @Override
    public PlantillaNotificacion buscarActivaPorTipo(String tipo) {
        return plantillaDataJpaRepository
                .findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
                        tipo, EstadoPlantilla.ACTIVA.name())
                .map(plantillaMapper::toPlantilla)
                .orElse(null);
    }

    @Override
    public List<PlantillaNotificacion> listarPlantillas() {
        return plantillaDataJpaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(plantillaMapper::toPlantilla)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarPlantilla(String idPlantilla) {
        plantillaDataJpaRepository.deleteById(idPlantilla);
    }
}
