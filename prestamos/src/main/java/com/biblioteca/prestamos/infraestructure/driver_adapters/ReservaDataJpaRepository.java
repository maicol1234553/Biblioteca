package com.biblioteca.prestamos.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaDataJpaRepository extends JpaRepository<ReservaData, String> {

    boolean existsByIdUsuarioAndIdLibroAndEstado(String idUsuario, String idLibro, String estado);

    Optional<ReservaData> findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
            String idUsuario, String idLibro, String estado);

    List<ReservaData> findByIdUsuarioOrderByFechaReservaDesc(String idUsuario);

    List<ReservaData> findByIdLibroOrderByFechaReservaAsc(String idLibro);

    List<ReservaData> findByIdLibroAndEstadoOrderByFechaReservaAsc(String idLibro, String estado);

    List<ReservaData> findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
            String estado, LocalDateTime fecha);

    List<ReservaData> findByEstadoOrderByFechaReservaAsc(String estado);

    List<ReservaData> findAllByOrderByFechaReservaDesc();
}
