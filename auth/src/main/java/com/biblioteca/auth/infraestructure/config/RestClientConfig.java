package com.biblioteca.auth.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean("notificationRestClient")
    public RestClient notificationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(requestFactory);

        // URL por defecto para desarrollo local; en Render se sobreescribe con
        // la variable de entorno NOTIFICACION_URL al aplicar el Blueprint.
        builder.baseUrl("http://localhost:8084");

        if (System.getenv("NOTIFICACION_URL") != null && !System.getenv("NOTIFICACION_URL").isBlank()) {
            builder.baseUrl(System.getenv("NOTIFICACION_URL"));
        }

        return builder.build();
    }
}