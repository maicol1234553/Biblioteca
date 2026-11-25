package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.exception.RecursoEnUsoException;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.infraestructure.mapper.NotificacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificacionDataGatewayImpl implements NotificacionGateway {

    private final NotificacionDataJpaRepository notificacionDataJpaRepository;
    private final NotificacionMapper notificacionMapper;

    @Override
    @Transactional
    public Notificacion guardarNotificacion(Notificacion notificacion) {
        return notificacionMapper.toNotificacion(
                notificacionDataJpaRepository.save(
                        notificacionMapper.toNotificacionData(notificacion)));
    }

    @Override
    @Transactional
    public Notificacion actualizarNotificacion(Notificacion notificacion) {
        return notificacionMapper.toNotificacion(
                notificacionDataJpaRepository.save(
                        notificacionMapper.toNotificacionData(notificacion)));
    }

    @Override
    public Notificacion buscarNotificacionPorId(String idNotificacion) {
        return notificacionDataJpaRepository.findById(idNotificacion)
                .map(notificacionMapper::toNotificacion)
                .orElse(null);
    }

    @Override
    public List<Notificacion> listarNotificaciones() {
        return notificacionDataJpaRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(notificacionMapper::toNotificacion)
                .toList();
    }

    @Override
    public List<Notificacion> listarPorUsuario(String idUsuario) {
        return notificacionDataJpaRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .map(notificacionMapper::toNotificacion)
                .toList();
    }

    @Override
    public List<Notificacion> listarPorEstado(String estado) {
        return notificacionDataJpaRepository.findByEstadoOrderByFechaCreacionDesc(estado)
                .stream()
                .map(notificacionMapper::toNotificacion)
                .toList();
    }

    @Override
    public List<Notificacion> listarPorEstados(Collection<String> estados) {
        return notificacionDataJpaRepository.findByEstadoInOrderByFechaCreacionDesc(estados)
                .stream()
                .map(notificacionMapper::toNotificacion)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarNotificacion(String idNotificacion) {
        try {
            notificacionDataJpaRepository.deleteById(idNotificacion);
            notificacionDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la notificacion porque tiene historial asociado");
        }
    }
}
