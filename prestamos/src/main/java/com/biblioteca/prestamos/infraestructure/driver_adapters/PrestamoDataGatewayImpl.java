package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoPrestamo;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.infraestructure.mapper.PrestamoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PrestamoDataGatewayImpl implements PrestamoGateway {

    private static final List<String> ESTADOS_ACTIVOS = List.of(
            EstadoPrestamo.ACTIVO.name(),
            EstadoPrestamo.VENCIDO.name()
    );

    private final PrestamoDataJpaRepository prestamoDataJpaRepository;
    private final PrestamoMapper prestamoMapper;

    @Override
    @Transactional
    public Prestamo guardarPrestamo(Prestamo prestamo) {
        return prestamoMapper.toPrestamo(
                prestamoDataJpaRepository.save(prestamoMapper.toPrestamoData(prestamo)));
    }

    @Override
    @Transactional
    public Prestamo actualizarPrestamo(Prestamo prestamo) {
        return prestamoMapper.toPrestamo(
                prestamoDataJpaRepository.save(prestamoMapper.toPrestamoData(prestamo)));
    }

    @Override
    public Prestamo buscarPrestamoPorId(String idPrestamo) {
        return prestamoDataJpaRepository.findById(idPrestamo)
                .map(prestamoMapper::toPrestamo)
                .orElse(null);
    }

    @Override
    public List<Prestamo> listarPrestamos() {
        return prestamoDataJpaRepository.findAllByOrderByFechaPrestamoDesc()
                .stream()
                .map(prestamoMapper::toPrestamo)
                .toList();
    }

    @Override
    public List<Prestamo> listarPrestamosActivosPorUsuario(String idUsuario) {
        return prestamoDataJpaRepository
                .findByIdUsuarioAndEstadoInOrderByFechaPrestamoDesc(idUsuario, ESTADOS_ACTIVOS)
                .stream()
                .map(prestamoMapper::toPrestamo)
                .toList();
    }

    @Override
    public List<Prestamo> listarHistorialPorUsuario(String idUsuario) {
        return prestamoDataJpaRepository.findByIdUsuarioOrderByFechaPrestamoDesc(idUsuario)
                .stream()
                .map(prestamoMapper::toPrestamo)
                .toList();
    }

    @Override
    public long contarPrestamosActivosPorUsuario(String idUsuario) {
        return prestamoDataJpaRepository.countByIdUsuarioAndEstadoIn(idUsuario, ESTADOS_ACTIVOS);
    }

    @Override
    public boolean existePrestamoActivoPorEjemplar(String idEjemplar) {
        return prestamoDataJpaRepository.existsByIdEjemplarAndEstadoIn(idEjemplar, ESTADOS_ACTIVOS);
    }

    @Override
    public List<Prestamo> listarPrestamosVencidos(LocalDateTime fecha) {
        return prestamoDataJpaRepository
                .findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
                        EstadoPrestamo.ACTIVO.name(), fecha)
                .stream()
                .map(prestamoMapper::toPrestamo)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarPrestamo(String idPrestamo) {
        try {
            prestamoDataJpaRepository.deleteById(idPrestamo);
            prestamoDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar el prestamo porque tiene devoluciones, multas o renovaciones asociadas");
        }
    }
}
