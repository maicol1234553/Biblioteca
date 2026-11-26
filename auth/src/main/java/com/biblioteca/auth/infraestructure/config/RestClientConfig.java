package com.biblioteca.auth.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${auth.servicios.notificacion-url:http://localhost:8084}")
    private String notificacionUrl;

    @Value("${auth.servicios.internal-api-key:}")
    private String internalApiKey;

    @Value("${auth.servicios.connect-timeout-segundos:10}")
    private int connectTimeoutSegundos;

    @Value("${auth.servicios.read-timeout-segundos:90}")
    private int readTimeoutSegundos;

    @Bean("notificationRestClient")
    public RestClient notificationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSegundos));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSegundos));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(notificacionUrl)
                .requestFactory(requestFactory);

        if (internalApiKey != null && !internalApiKey.isBlank()) {
            builder.defaultHeader("X-Internal-Api-Key", internalApiKey);
        }

        return builder.build();
    }
}
