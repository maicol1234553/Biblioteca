package com.biblioteca.prestamos.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MultaDataJpaRepository extends JpaRepository<MultaData, String> {

    List<MultaData> findByPrestamo_IdPrestamoOrderByFechaGeneracionDesc(String idPrestamo);

    List<MultaData> findByPrestamo_IdUsuarioOrderByFechaGeneracionDesc(String idUsuario);

    List<MultaData> findByPrestamo_IdUsuarioAndEstadoOrderByFechaGeneracionDesc(
            String idUsuario, String estado);

    Optional<MultaData> findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
            String idPrestamo, String estado);

    List<MultaData> findAllByOrderByFechaGeneracionDesc();
}
