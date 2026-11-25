package com.biblioteca.notificacion.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificacionDataJpaRepository extends JpaRepository<NotificacionData, String> {

    List<NotificacionData> findByIdUsuarioOrderByFechaCreacionDesc(String idUsuario);

    List<NotificacionData> findByEstadoOrderByFechaCreacionDesc(String estado);

    List<NotificacionData> findByEstadoInOrderByFechaCreacionDesc(Collection<String> estados);

    List<NotificacionData> findAllByOrderByFechaCreacionDesc();
}
