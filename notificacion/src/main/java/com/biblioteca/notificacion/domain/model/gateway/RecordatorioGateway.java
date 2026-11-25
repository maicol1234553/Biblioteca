package com.biblioteca.notificacion.domain.model.gateway;

import com.biblioteca.notificacion.domain.model.Recordatorio;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordatorioGateway {

    Recordatorio guardarRecordatorio(Recordatorio recordatorio);

    Recordatorio actualizarRecordatorio(Recordatorio recordatorio);

    Recordatorio buscarRecordatorioPorId(String idRecordatorio);

    List<Recordatorio> listarRecordatorios();

    List<Recordatorio> listarPorUsuario(String idUsuario);

    List<Recordatorio> listarPendientes();

    List<Recordatorio> listarPendientesHasta(LocalDateTime fecha);

    boolean existePendiente(String idUsuario, String idPrestamo, String tipo);

    void eliminarRecordatorio(String idRecordatorio);
}
