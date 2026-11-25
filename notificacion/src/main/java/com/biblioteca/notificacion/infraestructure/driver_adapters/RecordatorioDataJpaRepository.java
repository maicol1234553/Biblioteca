package com.biblioteca.notificacion.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordatorioDataJpaRepository extends JpaRepository<RecordatorioData, String> {

    List<RecordatorioData> findByIdUsuarioOrderByFechaProgramadaDesc(String idUsuario);

    List<RecordatorioData> findByEstadoOrderByFechaProgramadaAsc(String estado);

    List<RecordatorioData> findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
            String estado, LocalDateTime fecha);

    List<RecordatorioData> findAllByOrderByFechaProgramadaDesc();

    boolean existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
            String idUsuario, String idPrestamo, String tipo, String estado);
}
