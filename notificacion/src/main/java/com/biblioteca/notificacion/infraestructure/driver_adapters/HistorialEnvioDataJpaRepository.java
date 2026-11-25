package com.biblioteca.notificacion.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEnvioDataJpaRepository extends JpaRepository<HistorialEnvioData, String> {

    List<HistorialEnvioData> findByNotificacion_IdNotificacionOrderByFechaIntentoDesc(
            String idNotificacion);

    long countByNotificacion_IdNotificacion(String idNotificacion);

    void deleteByNotificacion_IdNotificacion(String idNotificacion);
}
