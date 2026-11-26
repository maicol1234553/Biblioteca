package com.biblioteca.notificacion.aplication;

import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.RecordatorioGateway;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class NotificacionConfigTest {

    private final NotificacionConfig config = new NotificacionConfig();

    private NotificacionProperties properties() {
        return new NotificacionProperties(
                "no-reply@biblioteca.local",
                new NotificacionProperties.Reintentos(3, 2000),
                new NotificacionProperties.Job(60000),
                new NotificacionProperties.Recordatorio(1),
                new NotificacionProperties.Servicios("http://auth:8081", 5),
                new NotificacionProperties.CorreoApi(null, null, null, null)
        );
    }

    @Test
    void creaPoliticaYReloj() {
        assertNotNull(config.politicaNotificaciones(properties()));
        assertNotNull(config.clock());
    }

    @Test
    void creaTodosLosCasosDeUsoConSusDependencias() {
        NotificacionProperties properties = properties();
        NotificacionGateway notificacionGateway = mock(NotificacionGateway.class);
        HistorialEnvioGateway historialEnvioGateway = mock(HistorialEnvioGateway.class);
        PlantillaNotificacionGateway plantillaGateway = mock(PlantillaNotificacionGateway.class);
        AuthGateway authGateway = mock(AuthGateway.class);
        CorreoGateway correoGateway = mock(CorreoGateway.class);
        RecordatorioGateway recordatorioGateway = mock(RecordatorioGateway.class);

        var notificacionUseCase = config.notificacionUseCase(
                notificacionGateway,
                historialEnvioGateway,
                plantillaGateway,
                authGateway,
                correoGateway,
                config.politicaNotificaciones(properties),
                config.clock()
        );

        assertNotNull(notificacionUseCase);
        assertNotNull(config.recordatorioUseCase(
                recordatorioGateway,
                notificacionUseCase,
                config.politicaNotificaciones(properties),
                config.clock()
        ));
        assertNotNull(config.plantillaUseCase(plantillaGateway));
    }
}
