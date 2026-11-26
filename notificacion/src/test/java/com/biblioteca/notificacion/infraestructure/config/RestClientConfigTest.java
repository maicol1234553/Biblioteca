package com.biblioteca.notificacion.infraestructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestClientConfigTest {

    @Test
    void authRestClient_debeCrearClienteConLasPropiedades() {
        // Arrange
        NotificacionProperties properties = mock(NotificacionProperties.class);
        NotificacionProperties.Servicios servicios =
                mock(NotificacionProperties.Servicios.class);

        when(properties.servicios()).thenReturn(servicios);
        when(servicios.authUrl()).thenReturn("http://localhost:8081");
        when(servicios.timeoutSegundos()).thenReturn(Math.toIntExact(5L));

        RestClientConfig config = new RestClientConfig();

        // Act
        RestClient resultado = config.authRestClient(properties);

        // Assert
        assertNotNull(resultado);
    }
}