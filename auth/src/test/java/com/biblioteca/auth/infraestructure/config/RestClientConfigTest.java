package com.biblioteca.auth.infraestructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientConfigTest {

    @Test
    void creaClienteConUrlYHeaderInterno() {
        RestClientConfig config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "notificacionUrl", "http://localhost:8084");
        ReflectionTestUtils.setField(config, "internalApiKey", "clave-interna");
        ReflectionTestUtils.setField(config, "connectTimeoutSegundos", 10);
        ReflectionTestUtils.setField(config, "readTimeoutSegundos", 90);

        RestClient cliente = config.notificationRestClient();

        assertNotNull(cliente);
    }

    @Test
    void creaClienteSinHeaderCuandoLaClaveEstaVacia() {
        RestClientConfig config = new RestClientConfig();
        ReflectionTestUtils.setField(config, "notificacionUrl", "http://localhost:8084");
        ReflectionTestUtils.setField(config, "internalApiKey", "");
        ReflectionTestUtils.setField(config, "connectTimeoutSegundos", 10);
        ReflectionTestUtils.setField(config, "readTimeoutSegundos", 90);

        RestClient cliente = config.notificationRestClient();

        assertNotNull(cliente);
    }
}
