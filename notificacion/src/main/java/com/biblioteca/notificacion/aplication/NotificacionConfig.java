package com.biblioteca.notificacion.aplication;

import com.biblioteca.notificacion.domain.model.PoliticaNotificaciones;
import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import com.biblioteca.notificacion.domain.usecase.NotificacionUseCase;
import com.biblioteca.notificacion.domain.usecase.PlantillaUseCase;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(NotificacionProperties.class)
public class NotificacionConfig {

    @Bean
    public PoliticaNotificaciones politicaNotificaciones(NotificacionProperties properties) {
        return new PoliticaNotificaciones(
                properties.reintentos().maximos(),
                properties.recordatorio().diasAnticipacion()
        );
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public NotificacionUseCase notificacionUseCase(NotificacionGateway notificacionGateway,
                                                   HistorialEnvioGateway historialEnvioGateway,
                                                   PlantillaNotificacionGateway plantillaGateway,
                                                   AuthGateway authGateway,
                                                   CorreoGateway correoGateway,
                                                   PoliticaNotificaciones politicaNotificaciones,
                                                   Clock clock) {
        return new NotificacionUseCase(
                notificacionGateway,
                historialEnvioGateway,
                plantillaGateway,
                authGateway,
                correoGateway,
                politicaNotificaciones,
                clock
        );
    }

    @Bean
    public RecordatorioUseCase recordatorioUseCase(RecordatorioGateway recordatorioGateway,
                                                   NotificacionUseCase notificacionUseCase,
                                                   PoliticaNotificaciones politicaNotificaciones,
                                                   Clock clock) {
        return new RecordatorioUseCase(
                recordatorioGateway,
                notificacionUseCase,
                politicaNotificaciones,
                clock
        );
    }

    @Bean
    public PlantillaUseCase plantillaUseCase(PlantillaNotificacionGateway plantillaGateway) {
        return new PlantillaUseCase(plantillaGateway);
    }
}
