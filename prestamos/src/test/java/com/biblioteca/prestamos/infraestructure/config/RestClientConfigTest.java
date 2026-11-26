package com.biblioteca.prestamos.infraestructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientConfigTest {

    private final RestClientConfig config = new RestClientConfig();

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
    void creaLosTresClientesRest() {
        PrestamosProperties properties = properties();

        RestClient auth = config.authRestClient(properties);
        RestClient catalogo = config.catalogoRestClient(properties);
        RestClient notificacion = config.notificacionRestClient(properties);

        assertNotNull(auth);
        assertNotNull(catalogo);
        assertNotNull(notificacion);
    }
}
