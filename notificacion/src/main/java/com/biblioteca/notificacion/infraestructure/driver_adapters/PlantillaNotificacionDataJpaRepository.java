package com.biblioteca.notificacion.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantillaNotificacionDataJpaRepository
        extends JpaRepository<PlantillaNotificacionData, String> {

    Optional<PlantillaNotificacionData> findFirstByNombreIgnoreCase(String nombre);

    Optional<PlantillaNotificacionData> findFirstByTipoIgnoreCaseAndEstadoOrderByNombreAsc(
            String tipo, String estado);

    List<PlantillaNotificacionData> findAllByOrderByNombreAsc();
}
