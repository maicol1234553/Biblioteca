package com.biblioteca.prestamos.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean("authRestClient")
    public RestClient authRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().authUrl(), properties.servicios().timeoutSegundos());
    }

    @Bean("catalogoRestClient")
    public RestClient catalogoRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().catalogoUrl(), properties.servicios().timeoutSegundos());
    }

    @Bean("notificacionRestClient")
    public RestClient notificacionRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().notificacionUrl(), properties.servicios().timeoutSegundos());
    }

    private RestClient construir(String baseUrl, int timeoutSegundos) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSegundos));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSegundos));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
