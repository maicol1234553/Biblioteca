package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.infraestructure.mapper.DevolucionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DevolucionDataGatewayImpl implements DevolucionGateway {

    private final DevolucionDataJpaRepository devolucionDataJpaRepository;
    private final PrestamoDataJpaRepository prestamoDataJpaRepository;
    private final DevolucionMapper devolucionMapper;

    @Override
    @Transactional
    public Devolucion guardarDevolucion(Devolucion devolucion) {
        return devolucionMapper.toDevolucion(
                devolucionDataJpaRepository.save(devolucionMapper.toDevolucionData(
                        devolucion, buscarPrestamoData(devolucion.getIdPrestamo()))));
    }

    @Override
    public Devolucion buscarDevolucionPorId(String idDevolucion) {
        return devolucionDataJpaRepository.findById(idDevolucion)
                .map(devolucionMapper::toDevolucion)
                .orElse(null);
    }

    @Override
    public Devolucion buscarDevolucionPorPrestamo(String idPrestamo) {
        return devolucionDataJpaRepository.findByPrestamo_IdPrestamo(idPrestamo)
                .map(devolucionMapper::toDevolucion)
                .orElse(null);
    }

    @Override
    public List<Devolucion> listarDevoluciones() {
        return devolucionDataJpaRepository.findAllByOrderByFechaDevolucionDesc()
                .stream()
                .map(devolucionMapper::toDevolucion)
                .toList();
    }

    @Override
    public List<Devolucion> listarDevolucionesPorPrestamo(String idPrestamo) {
        return devolucionDataJpaRepository
                .findByPrestamo_IdPrestamoOrderByFechaDevolucionDesc(idPrestamo)
                .stream()
                .map(devolucionMapper::toDevolucion)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarDevolucion(String idDevolucion) {
        DevolucionData devolucion = devolucionDataJpaRepository.findById(idDevolucion)
                .orElse(null);

        if (devolucion == null) {
            return;
        }

        try {




            PrestamoData prestamo = devolucion.getPrestamo();

            if (prestamo != null) {
                prestamo.setDevolucion(null);
                prestamoDataJpaRepository.save(prestamo);
            }

            devolucionDataJpaRepository.delete(devolucion);
            devolucionDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la devolucion porque tiene informacion asociada");
        }
    }

    private PrestamoData buscarPrestamoData(String idPrestamo) {
        return prestamoDataJpaRepository.findById(idPrestamo)
                .orElseThrow(() -> new PrestamoNoEncontradoException(
                        "Prestamo no encontrado con id: " + idPrestamo));
    }
}
