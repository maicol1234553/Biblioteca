package com.biblioteca.prestamos.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenovacionDataJpaRepository extends JpaRepository<RenovacionData, String> {

    List<RenovacionData> findByPrestamo_IdPrestamoOrderByFechaSolicitudDesc(String idPrestamo);

    long countByPrestamo_IdPrestamo(String idPrestamo);

    List<RenovacionData> findAllByOrderByFechaSolicitudDesc();
}
