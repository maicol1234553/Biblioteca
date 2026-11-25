package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.model.EstadoRecordatorio;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import com.biblioteca.notificacion.infraestructure.mapper.RecordatorioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordatorioDataGatewayImpl implements RecordatorioGateway {

    private final RecordatorioDataJpaRepository recordatorioDataJpaRepository;
    private final RecordatorioMapper recordatorioMapper;

    @Override
    @Transactional
    public Recordatorio guardarRecordatorio(Recordatorio recordatorio) {
        return recordatorioMapper.toRecordatorio(
                recordatorioDataJpaRepository.save(
                        recordatorioMapper.toRecordatorioData(recordatorio)));
    }

    @Override
    @Transactional
    public Recordatorio actualizarRecordatorio(Recordatorio recordatorio) {
        return recordatorioMapper.toRecordatorio(
                recordatorioDataJpaRepository.save(
                        recordatorioMapper.toRecordatorioData(recordatorio)));
    }

    @Override
    public Recordatorio buscarRecordatorioPorId(String idRecordatorio) {
        return recordatorioDataJpaRepository.findById(idRecordatorio)
                .map(recordatorioMapper::toRecordatorio)
                .orElse(null);
    }

    @Override
    public List<Recordatorio> listarRecordatorios() {
        return recordatorioDataJpaRepository.findAllByOrderByFechaProgramadaDesc()
                .stream()
                .map(recordatorioMapper::toRecordatorio)
                .toList();
    }

    @Override
    public List<Recordatorio> listarPorUsuario(String idUsuario) {
        return recordatorioDataJpaRepository
                .findByIdUsuarioOrderByFechaProgramadaDesc(idUsuario)
                .stream()
                .map(recordatorioMapper::toRecordatorio)
                .toList();
    }

    @Override
    public List<Recordatorio> listarPendientes() {
        return recordatorioDataJpaRepository
                .findByEstadoOrderByFechaProgramadaAsc(EstadoRecordatorio.PENDIENTE.name())
                .stream()
                .map(recordatorioMapper::toRecordatorio)
                .toList();
    }

    @Override
    public List<Recordatorio> listarPendientesHasta(LocalDateTime fecha) {
        return recordatorioDataJpaRepository
                .findByEstadoAndFechaProgramadaLessThanEqualOrderByFechaProgramadaAsc(
                        EstadoRecordatorio.PENDIENTE.name(), fecha)
                .stream()
                .map(recordatorioMapper::toRecordatorio)
                .toList();
    }

    @Override
    public boolean existePendiente(String idUsuario, String idPrestamo, String tipo) {
        return recordatorioDataJpaRepository.existsByIdUsuarioAndIdPrestamoAndTipoAndEstado(
                idUsuario, idPrestamo, tipo, EstadoRecordatorio.PENDIENTE.name());
    }

    @Override
    @Transactional
    public void eliminarRecordatorio(String idRecordatorio) {
        recordatorioDataJpaRepository.deleteById(idRecordatorio);
    }
}
