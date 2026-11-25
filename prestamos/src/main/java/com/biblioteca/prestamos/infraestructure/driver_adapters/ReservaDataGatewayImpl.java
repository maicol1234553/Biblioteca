package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoReserva;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import com.biblioteca.prestamos.infraestructure.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservaDataGatewayImpl implements ReservaGateway {

    private final ReservaDataJpaRepository reservaDataJpaRepository;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public Reserva guardarReserva(Reserva reserva) {
        return reservaMapper.toReserva(
                reservaDataJpaRepository.save(reservaMapper.toReservaData(reserva)));
    }

    @Override
    @Transactional
    public Reserva actualizarReserva(Reserva reserva) {
        return reservaMapper.toReserva(
                reservaDataJpaRepository.save(reservaMapper.toReservaData(reserva)));
    }

    @Override
    public Reserva buscarReservaPorId(String idReserva) {
        return reservaDataJpaRepository.findById(idReserva)
                .map(reservaMapper::toReserva)
                .orElse(null);
    }

    @Override
    public List<Reserva> listarReservas() {
        return reservaDataJpaRepository.findAllByOrderByFechaReservaDesc()
                .stream()
                .map(reservaMapper::toReserva)
                .toList();
    }

    @Override
    public List<Reserva> listarReservasPorUsuario(String idUsuario) {
        return reservaDataJpaRepository.findByIdUsuarioOrderByFechaReservaDesc(idUsuario)
                .stream()
                .map(reservaMapper::toReserva)
                .toList();
    }

    @Override
    public List<Reserva> listarReservasPorLibro(String idLibro) {
        return reservaDataJpaRepository.findByIdLibroOrderByFechaReservaAsc(idLibro)
                .stream()
                .map(reservaMapper::toReserva)
                .toList();
    }

    @Override
    public List<Reserva> listarReservasPendientesPorLibro(String idLibro) {
        return reservaDataJpaRepository
                .findByIdLibroAndEstadoOrderByFechaReservaAsc(
                        idLibro, EstadoReserva.PENDIENTE.name())
                .stream()
                .map(reservaMapper::toReserva)
                .toList();
    }

    @Override
    public Reserva buscarReservaPendiente(String idUsuario, String idLibro) {
        return reservaDataJpaRepository
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name())
                .map(reservaMapper::toReserva)
                .orElse(null);
    }

    @Override
    public boolean existeReservaPendiente(String idUsuario, String idLibro) {
        return reservaDataJpaRepository.existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
    }

    @Override
    public List<Reserva> listarReservasPendientesExpiradas(LocalDateTime fecha) {
        return reservaDataJpaRepository
                .findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
                        EstadoReserva.PENDIENTE.name(), fecha)
                .stream()
                .map(reservaMapper::toReserva)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarReserva(String idReserva) {
        try {
            reservaDataJpaRepository.deleteById(idReserva);
            reservaDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la reserva porque tiene informacion asociada");
        }
    }
}
