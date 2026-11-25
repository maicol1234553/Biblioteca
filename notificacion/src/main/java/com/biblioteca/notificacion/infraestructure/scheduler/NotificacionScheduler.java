package com.biblioteca.notificacion.infraestructure.scheduler;

import com.biblioteca.notificacion.domain.usecase.NotificacionUseCase;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificacionScheduler.class);

    private final NotificacionUseCase notificacionUseCase;
    private final RecordatorioUseCase recordatorioUseCase;

    @Scheduled(
            fixedDelayString = "${notificaciones.job.intervalo-ms:60000}",
            initialDelayString = "${notificaciones.job.initial-delay-ms:30000}"
    )
    public void procesarPendientes() {
        try {
            int reintentos = notificacionUseCase.reintentarFallidas().size();
            int recordatorios = recordatorioUseCase.procesarRecordatoriosPendientes().size();

            if (reintentos > 0 || recordatorios > 0) {
                log.info("Proceso de notificaciones: {} reintentos, {} recordatorios procesados",
                        reintentos, recordatorios);
            }
        } catch (RuntimeException exception) {
            log.error("No se pudo ejecutar el proceso de notificaciones", exception);
        }
    }
}
