package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.infraestructure.mapper.HistorialEnvioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HistorialEnvioDataGatewayImpl implements HistorialEnvioGateway {

    private final HistorialEnvioDataJpaRepository historialEnvioDataJpaRepository;
    private final NotificacionDataJpaRepository notificacionDataJpaRepository;
    private final HistorialEnvioMapper historialEnvioMapper;

    @Override
    @Transactional
    public HistorialEnvio guardarHistorial(HistorialEnvio historial) {
        NotificacionData notificacion = notificacionDataJpaRepository
                .findById(historial.getIdNotificacion())
                .orElseThrow(() -> new NotificacionNoEncontradaException(
                        "Notificacion no encontrada con id: " + historial.getIdNotificacion()));

        return historialEnvioMapper.toHistorial(
                historialEnvioDataJpaRepository.save(
                        historialEnvioMapper.toHistorialData(historial, notificacion)));
    }

    @Override
    public List<HistorialEnvio> listarPorNotificacion(String idNotificacion) {
        return historialEnvioDataJpaRepository
                .findByNotificacion_IdNotificacionOrderByFechaIntentoDesc(idNotificacion)
                .stream()
                .map(historialEnvioMapper::toHistorial)
                .toList();
    }

    @Override
    public long contarPorNotificacion(String idNotificacion) {
        return historialEnvioDataJpaRepository
                .countByNotificacion_IdNotificacion(idNotificacion);
    }

    @Override
    @Transactional
    public void eliminarPorNotificacion(String idNotificacion) {
        historialEnvioDataJpaRepository
                .deleteByNotificacion_IdNotificacion(idNotificacion);
    }
}
