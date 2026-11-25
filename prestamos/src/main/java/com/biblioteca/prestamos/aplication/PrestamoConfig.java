package com.biblioteca.prestamos.aplication;

import com.biblioteca.prestamos.domain.model.PoliticaPrestamos;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.RenovacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import com.biblioteca.prestamos.domain.usecase.DevolucionUseCase;
import com.biblioteca.prestamos.domain.usecase.MultaUseCase;
import com.biblioteca.prestamos.domain.usecase.PrestamoUseCase;
import com.biblioteca.prestamos.domain.usecase.RenovacionUseCase;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(PrestamosProperties.class)
public class PrestamoConfig {

    @Bean
    public PoliticaPrestamos politicaPrestamos(PrestamosProperties properties) {
        return new PoliticaPrestamos(
                properties.limitePrestamosActivos(),
                properties.diasPrestamo(),
                properties.diasReserva(),
                properties.maxRenovaciones(),
                properties.multaPorDia()
        );
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public PrestamoUseCase prestamoUseCase(PrestamoGateway prestamoGateway,
                                           ReservaGateway reservaGateway,
                                           MultaGateway multaGateway,
                                           DevolucionGateway devolucionGateway,
                                           AuthGateway authGateway,
                                           CatalogoGateway catalogoGateway,
                                           NotificacionGateway notificacionGateway,
                                           PoliticaPrestamos politicaPrestamos,
                                           Clock clock) {
        return new PrestamoUseCase(
                prestamoGateway,
                reservaGateway,
                multaGateway,
                devolucionGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                politicaPrestamos,
                clock
        );
    }

    @Bean
    public ReservaUseCase reservaUseCase(ReservaGateway reservaGateway,
                                         AuthGateway authGateway,
                                         CatalogoGateway catalogoGateway,
                                         NotificacionGateway notificacionGateway,
                                         PoliticaPrestamos politicaPrestamos,
                                         Clock clock) {
        return new ReservaUseCase(
                reservaGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                politicaPrestamos,
                clock
        );
    }

    @Bean
    public RenovacionUseCase renovacionUseCase(PrestamoGateway prestamoGateway,
                                               ReservaGateway reservaGateway,
                                               RenovacionGateway renovacionGateway,
                                               CatalogoGateway catalogoGateway,
                                               NotificacionGateway notificacionGateway,
                                               PoliticaPrestamos politicaPrestamos,
                                               Clock clock) {
        return new RenovacionUseCase(
                prestamoGateway,
                reservaGateway,
                renovacionGateway,
                catalogoGateway,
                notificacionGateway,
                politicaPrestamos,
                clock
        );
    }

    @Bean
    public MultaUseCase multaUseCase(MultaGateway multaGateway,
                                     PrestamoGateway prestamoGateway) {
        return new MultaUseCase(multaGateway, prestamoGateway);
    }

    @Bean
    public DevolucionUseCase devolucionUseCase(DevolucionGateway devolucionGateway,
                                               PrestamoGateway prestamoGateway) {
        return new DevolucionUseCase(devolucionGateway, prestamoGateway);
    }
}
