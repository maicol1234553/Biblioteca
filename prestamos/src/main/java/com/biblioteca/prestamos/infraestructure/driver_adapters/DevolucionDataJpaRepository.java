package com.biblioteca.prestamos.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DevolucionDataJpaRepository extends JpaRepository<DevolucionData, String> {

    Optional<DevolucionData> findByPrestamo_IdPrestamo(String idPrestamo);

    boolean existsByPrestamo_IdPrestamo(String idPrestamo);

    List<DevolucionData> findByPrestamo_IdPrestamoOrderByFechaDevolucionDesc(String idPrestamo);

    List<DevolucionData> findAllByOrderByFechaDevolucionDesc();
}
