package com.biblioteca.notificacion.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${notificaciones.servicios.connect-timeout-segundos:10}")
    private int connectTimeoutSegundos;

    @Bean("authRestClient")
    public RestClient authRestClient(NotificacionProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSegundos > 0 ? connectTimeoutSegundos : 10));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.servicios().timeoutSegundos()));

        return RestClient.builder()
                .baseUrl(properties.servicios().authUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
