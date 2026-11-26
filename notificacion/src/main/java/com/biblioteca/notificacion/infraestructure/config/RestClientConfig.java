package com.biblioteca.notificacion.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean("authRestClient")
    public RestClient authRestClient(NotificacionProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.servicios().timeoutSegundos()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.servicios().timeoutSegundos()));

        return RestClient.builder()
                .baseUrl(properties.servicios().authUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Cliente de la API de Brevo. La autenticacion va en la cabecera api-key
     * que exige el proveedor; nunca se imprime en log ni se persiste.
     */
    @Bean("brevoRestClient")
    public RestClient brevoRestClient(NotificacionProperties properties) {
        int timeout = properties.sms() == null || properties.sms().timeoutSegundos() <= 0
                ? 10
                : properties.sms().timeoutSegundos();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeout));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeout));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.brevo.com")
                .requestFactory(requestFactory);

        if (properties.sms() != null && properties.sms().apiKey() != null
                && !properties.sms().apiKey().isBlank()) {
            builder.defaultHeader("api-key", properties.sms().apiKey().trim());
        }

        return builder.build();
    }
}
