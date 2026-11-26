package com.biblioteca.prestamos.aplication;

import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.NotificacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import com.biblioteca.prestamos.domain.model.gateway.RenovacionGateway;
import com.biblioteca.prestamos.domain.model.gateway.ReservaGateway;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PrestamoConfigTest {

    private final PrestamoConfig config = new PrestamoConfig();

    private PrestamosProperties properties() {
        return new PrestamosProperties(
                3,
                15,
                3,
                2,
                new BigDecimal("2000.00"),
                new PrestamosProperties.Servicios(
                        "http://auth:8081",
                        "http://catalogo:8082",
                        "http://notificacion:8084",
                        "admin",
                        true,
                        "/api/notificaciones",
                        5
                ),
                new PrestamosProperties.Job(60000)
        );
    }

    @Test
    void creaPoliticaYReloj() {
        assertNotNull(config.politicaPrestamos(properties()));
        assertNotNull(config.clock());
    }

    @Test
    void creaTodosLosCasosDeUsoConSusDependencias() {
        PrestamosProperties properties = properties();
        PrestamoGateway prestamoGateway = mock(PrestamoGateway.class);
        ReservaGateway reservaGateway = mock(ReservaGateway.class);
        MultaGateway multaGateway = mock(MultaGateway.class);
        DevolucionGateway devolucionGateway = mock(DevolucionGateway.class);
        RenovacionGateway renovacionGateway = mock(RenovacionGateway.class);
        AuthGateway authGateway = mock(AuthGateway.class);
        CatalogoGateway catalogoGateway = mock(CatalogoGateway.class);
        NotificacionGateway notificacionGateway = mock(NotificacionGateway.class);
        Clock clock = config.clock();

        assertNotNull(config.prestamoUseCase(
                prestamoGateway,
                reservaGateway,
                multaGateway,
                devolucionGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                config.politicaPrestamos(properties),
                clock
        ));
        assertNotNull(config.reservaUseCase(
                reservaGateway,
                authGateway,
                catalogoGateway,
                notificacionGateway,
                config.politicaPrestamos(properties),
                clock
        ));
        assertNotNull(config.renovacionUseCase(
                prestamoGateway,
                reservaGateway,
                renovacionGateway,
                catalogoGateway,
                notificacionGateway,
                config.politicaPrestamos(properties),
                clock
        ));
        assertNotNull(config.multaUseCase(multaGateway, prestamoGateway));
        assertNotNull(config.devolucionUseCase(devolucionGateway, prestamoGateway));
    }
}
