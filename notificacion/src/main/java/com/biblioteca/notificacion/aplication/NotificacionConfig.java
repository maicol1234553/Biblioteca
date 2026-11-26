package com.biblioteca.notificacion.aplication;

import com.biblioteca.notificacion.domain.model.CanalNotificacion;
import com.biblioteca.notificacion.domain.model.PoliticaNotificaciones;
import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import com.biblioteca.notificacion.domain.model.gateway.SmsGateway;
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
        NotificacionProperties.Sms sms = properties.sms();
        String codigoPais = sms == null || sms.codigoPais() == null
                ? ""
                : sms.codigoPais().trim();
        CanalNotificacion canal = properties.canal() == null || properties.canal().isBlank()
                ? CanalNotificacion.SMS
                : CanalNotificacion.desde(properties.canal());

        return new PoliticaNotificaciones(
                properties.reintentos().maximos(),
                properties.recordatorio().diasAnticipacion(),
                codigoPais,
                canal
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
                                                   SmsGateway smsGateway,
                                                   PoliticaNotificaciones politicaNotificaciones,
                                                   Clock clock) {
        return new NotificacionUseCase(
                notificacionGateway,
                historialEnvioGateway,
                plantillaGateway,
                authGateway,
                correoGateway,
                smsGateway,
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
