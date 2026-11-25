package com.biblioteca.prestamos.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PrestamoDataJpaRepository extends JpaRepository<PrestamoData, String> {

    List<PrestamoData> findByIdUsuarioAndEstadoInOrderByFechaPrestamoDesc(
            String idUsuario, Collection<String> estados);

    List<PrestamoData> findByIdUsuarioOrderByFechaPrestamoDesc(String idUsuario);

    long countByIdUsuarioAndEstadoIn(String idUsuario, Collection<String> estados);

    boolean existsByIdEjemplarAndEstadoIn(String idEjemplar, Collection<String> estados);

    List<PrestamoData> findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
            String estado, LocalDateTime fecha);

    List<PrestamoData> findAllByOrderByFechaPrestamoDesc();
}
