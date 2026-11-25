package com.biblioteca.prestamos.infraestructure.scheduler;

import com.biblioteca.prestamos.domain.usecase.PrestamoUseCase;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrestamoVencimientoJob {

    private static final Logger log = LoggerFactory.getLogger(PrestamoVencimientoJob.class);

    private final PrestamoUseCase prestamoUseCase;
    private final ReservaUseCase reservaUseCase;

    @Scheduled(fixedDelayString = "${prestamos.job.intervalo-ms:3600000}")
    public void ejecutar() {
        try {
            int prestamosVencidos = prestamoUseCase.procesarPrestamosVencidos().size();
            int reservasExpiradas = reservaUseCase.procesarReservasExpiradas().size();

            if (prestamosVencidos > 0 || reservasExpiradas > 0) {
                log.info("Proceso de vencimientos: {} prestamos vencidos, {} reservas expiradas",
                        prestamosVencidos, reservasExpiradas);
            }
        } catch (RuntimeException exception) {
            log.error("No se pudo ejecutar el proceso de vencimientos", exception);
        }
    }
}
