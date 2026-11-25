package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.gateway.RenovacionGateway;
import com.biblioteca.prestamos.infraestructure.mapper.RenovacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RenovacionDataGatewayImpl implements RenovacionGateway {

    private final RenovacionDataJpaRepository renovacionDataJpaRepository;
    private final PrestamoDataJpaRepository prestamoDataJpaRepository;
    private final RenovacionMapper renovacionMapper;

    @Override
    @Transactional
    public Renovacion guardarRenovacion(Renovacion renovacion) {
        return renovacionMapper.toRenovacion(
                renovacionDataJpaRepository.save(renovacionMapper.toRenovacionData(
                        renovacion, buscarPrestamoData(renovacion.getIdPrestamo()))));
    }

    @Override
    public Renovacion buscarRenovacionPorId(String idRenovacion) {
        return renovacionDataJpaRepository.findById(idRenovacion)
                .map(renovacionMapper::toRenovacion)
                .orElse(null);
    }

    @Override
    public List<Renovacion> listarRenovaciones() {
        return renovacionDataJpaRepository.findAllByOrderByFechaSolicitudDesc()
                .stream()
                .map(renovacionMapper::toRenovacion)
                .toList();
    }

    @Override
    public List<Renovacion> listarRenovacionesPorPrestamo(String idPrestamo) {
        return renovacionDataJpaRepository
                .findByPrestamo_IdPrestamoOrderByFechaSolicitudDesc(idPrestamo)
                .stream()
                .map(renovacionMapper::toRenovacion)
                .toList();
    }

    @Override
    public long contarRenovacionesPorPrestamo(String idPrestamo) {
        return renovacionDataJpaRepository.countByPrestamo_IdPrestamo(idPrestamo);
    }

    @Override
    @Transactional
    public void eliminarRenovacion(String idRenovacion) {
        try {
            renovacionDataJpaRepository.deleteById(idRenovacion);
            renovacionDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la renovacion porque tiene informacion asociada");
        }
    }

    private PrestamoData buscarPrestamoData(String idPrestamo) {
        return prestamoDataJpaRepository.findById(idPrestamo)
                .orElseThrow(() -> new PrestamoNoEncontradoException(
                        "Prestamo no encontrado con id: " + idPrestamo));
    }
}
