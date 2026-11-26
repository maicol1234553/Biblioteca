package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioDuplicadoException;
import com.biblioteca.notificacion.domain.exception.RecordatorioNoEncontradoException;
import com.biblioteca.notificacion.domain.model.EstadoRecordatorio;
import com.biblioteca.notificacion.domain.model.PoliticaNotificaciones;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.model.TipoNotificacion;
import com.biblioteca.notificacion.domain.model.TipoRecordatorio;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RecordatorioUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioUseCase.class);

    private final RecordatorioGateway recordatorioGateway;
    private final NotificacionUseCase notificacionUseCase;
    private final PoliticaNotificaciones politica;
    private final Clock clock;

    public Recordatorio programarRecordatorioDevolucion(String idUsuario,
                                                        String idPrestamo,
                                                        LocalDateTime fechaProgramada,
                                                        String tipo) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (esBlanco(idPrestamo)) {
            throw new IllegalArgumentException("El prestamo es obligatorio");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        TipoRecordatorio tipoRecordatorio = TipoRecordatorio.desde(tipo);
        LocalDateTime fecha = fechaProgramada == null
                ? ahora.plusDays(politica.diasAnticipacionRecordatorio())
                : fechaProgramada;

        if (fecha.isBefore(ahora)) {
            throw new IllegalArgumentException(
                    "La fecha programada del recordatorio no puede ser pasada");
        }

        if (recordatorioGateway.existePendiente(
                idUsuario, idPrestamo, tipoRecordatorio.name())) {
            throw new RecordatorioDuplicadoException(
                    "Ya existe un recordatorio pendiente para este usuario y prestamo");
        }

        Recordatorio recordatorio = new Recordatorio();
        recordatorio.setIdRecordatorio(UUID.randomUUID().toString());
        recordatorio.setIdUsuario(idUsuario);
        recordatorio.setIdPrestamo(idPrestamo);
        recordatorio.setFechaProgramada(fecha);
        recordatorio.setTipo(tipoRecordatorio.name());
        recordatorio.setEstado(EstadoRecordatorio.PENDIENTE.name());

        return recordatorioGateway.guardarRecordatorio(recordatorio);
    }

    public Recordatorio cancelarRecordatorio(String idRecordatorio) {
        Recordatorio recordatorio = buscarRecordatorioInterno(idRecordatorio);

        if (!EstadoRecordatorio.PENDIENTE.name().equals(recordatorio.getEstado())) {
            throw new EstadoInvalidoException(
                    "Solo se pueden cancelar recordatorios pendientes");
        }

        recordatorio.setEstado(EstadoRecordatorio.CANCELADO.name());

        return recordatorioGateway.actualizarRecordatorio(recordatorio);
    }

    public List<Recordatorio> listarPendientes() {
        return recordatorioGateway.listarPendientes();
    }

    public List<Recordatorio> listarPorUsuario(String idUsuario) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        return recordatorioGateway.listarPorUsuario(idUsuario);
    }

    public List<Recordatorio> listarRecordatorios() {
        return recordatorioGateway.listarRecordatorios();
    }

    public Recordatorio buscarRecordatorio(String idRecordatorio) {
        return buscarRecordatorioInterno(idRecordatorio);
    }







    public List<Recordatorio> procesarRecordatoriosPendientes() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<Recordatorio> pendientes = recordatorioGateway.listarPendientesHasta(ahora);

        for (Recordatorio recordatorio : pendientes) {
            TipoRecordatorio tipo = TipoRecordatorio.desde(recordatorio.getTipo());
            TipoNotificacion tipoNotificacion = tipo == TipoRecordatorio.PRESTAMO_VENCIDO
                    ? TipoNotificacion.PRESTAMO_VENCIDO
                    : TipoNotificacion.DEVOLUCION_PROXIMA;
            String mensaje = tipo == TipoRecordatorio.PRESTAMO_VENCIDO
                    ? "Aviso: el prestamo " + recordatorio.getIdPrestamo() + " esta vencido."
                    : "Recordatorio: el prestamo " + recordatorio.getIdPrestamo()
                            + " esta proximo a vencer.";

            try {
                notificacionUseCase.enviarCorreo(
                        recordatorio.getIdUsuario(),
                        null,
                        null,
                        mensaje,
                        tipoNotificacion.name()
                );
            } catch (RuntimeException exception) {
                log.warn("No se pudo enviar el recordatorio {}: {}",
                        recordatorio.getIdRecordatorio(), exception.getMessage());
            }

            recordatorio.setEstado(EstadoRecordatorio.PROCESADO.name());
            recordatorioGateway.actualizarRecordatorio(recordatorio);
        }

        return pendientes;
    }

    public void eliminarRecordatorio(String idRecordatorio) {
        Recordatorio recordatorio = buscarRecordatorioInterno(idRecordatorio);

        if (EstadoRecordatorio.PENDIENTE.name().equals(recordatorio.getEstado())) {
            throw new EstadoInvalidoException(
                    "Cancele el recordatorio antes de eliminarlo");
        }

        recordatorioGateway.eliminarRecordatorio(idRecordatorio);
    }

    private Recordatorio buscarRecordatorioInterno(String idRecordatorio) {
        Recordatorio recordatorio = recordatorioGateway.buscarRecordatorioPorId(idRecordatorio);

        if (recordatorio == null) {
            throw new RecordatorioNoEncontradoException(
                    "Recordatorio no encontrado con id: " + idRecordatorio);
        }

        return recordatorio;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
