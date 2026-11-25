package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoMulta;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.infraestructure.mapper.MultaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MultaDataGatewayImpl implements MultaGateway {

    private final MultaDataJpaRepository multaDataJpaRepository;
    private final PrestamoDataJpaRepository prestamoDataJpaRepository;
    private final MultaMapper multaMapper;

    @Override
    @Transactional
    public Multa guardarMulta(Multa multa) {
        return multaMapper.toMulta(
                multaDataJpaRepository.save(multaMapper.toMultaData(
                        multa, buscarPrestamoData(multa.getIdPrestamo()))));
    }

    @Override
    @Transactional
    public Multa actualizarMulta(Multa multa) {
        return multaMapper.toMulta(
                multaDataJpaRepository.save(multaMapper.toMultaData(
                        multa, buscarPrestamoData(multa.getIdPrestamo()))));
    }

    @Override
    public Multa buscarMultaPorId(String idMulta) {
        return multaDataJpaRepository.findById(idMulta)
                .map(multaMapper::toMulta)
                .orElse(null);
    }

    @Override
    public List<Multa> listarMultas() {
        return multaDataJpaRepository.findAllByOrderByFechaGeneracionDesc()
                .stream()
                .map(multaMapper::toMulta)
                .toList();
    }

    @Override
    public List<Multa> listarMultasPorPrestamo(String idPrestamo) {
        return multaDataJpaRepository.findByPrestamo_IdPrestamoOrderByFechaGeneracionDesc(idPrestamo)
                .stream()
                .map(multaMapper::toMulta)
                .toList();
    }

    @Override
    public List<Multa> listarMultasPorUsuario(String idUsuario) {
        return multaDataJpaRepository.findByPrestamo_IdUsuarioOrderByFechaGeneracionDesc(idUsuario)
                .stream()
                .map(multaMapper::toMulta)
                .toList();
    }

    @Override
    public List<Multa> listarMultasPendientesPorUsuario(String idUsuario) {
        return multaDataJpaRepository
                .findByPrestamo_IdUsuarioAndEstadoOrderByFechaGeneracionDesc(
                        idUsuario, EstadoMulta.PENDIENTE.name())
                .stream()
                .map(multaMapper::toMulta)
                .toList();
    }

    @Override
    public Multa buscarMultaPendientePorPrestamo(String idPrestamo) {
        return multaDataJpaRepository
                .findFirstByPrestamo_IdPrestamoAndEstadoOrderByFechaGeneracionDesc(
                        idPrestamo, EstadoMulta.PENDIENTE.name())
                .map(multaMapper::toMulta)
                .orElse(null);
    }

    @Override
    @Transactional
    public void eliminarMulta(String idMulta) {
        try {
            multaDataJpaRepository.deleteById(idMulta);
            multaDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la multa porque tiene informacion asociada");
        }
    }

    private PrestamoData buscarPrestamoData(String idPrestamo) {
        return prestamoDataJpaRepository.findById(idPrestamo)
                .orElseThrow(() -> new PrestamoNoEncontradoException(
                        "Prestamo no encontrado con id: " + idPrestamo));
    }
}
