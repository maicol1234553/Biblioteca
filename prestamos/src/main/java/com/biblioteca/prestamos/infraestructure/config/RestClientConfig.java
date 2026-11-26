package com.biblioteca.prestamos.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${prestamos.servicios.connect-timeout-segundos:10}")
    private int connectTimeoutSegundos;

    @Value("${prestamos.servicios.internal-api-key:}")
    private String internalApiKey;

    @Bean("authRestClient")
    public RestClient authRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().authUrl(), properties.servicios().timeoutSegundos(), false);
    }

    @Bean("catalogoRestClient")
    public RestClient catalogoRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().catalogoUrl(), properties.servicios().timeoutSegundos(), false);
    }

    @Bean("notificacionRestClient")
    public RestClient notificacionRestClient(PrestamosProperties properties) {
        return construir(properties.servicios().notificacionUrl(), properties.servicios().timeoutSegundos(), true);
    }

    private RestClient construir(String baseUrl, int readTimeoutSegundos, boolean incluirApiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSegundos > 0 ? connectTimeoutSegundos : 10));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSegundos));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory);

        if (incluirApiKey && internalApiKey != null && !internalApiKey.isBlank()) {
            builder.defaultHeader("X-Internal-Api-Key", internalApiKey);
        }

        return builder.build();
    }
}
