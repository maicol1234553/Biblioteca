package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthGatewayImpl implements AuthGateway {

    private final RestClient authRestClient;
    private final ObjectMapper objectMapper;

    public AuthGatewayImpl(@Qualifier("authRestClient") RestClient authRestClient,
                           ObjectMapper objectMapper) {
        this.authRestClient = authRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public UsuarioExterno buscarUsuario(String idUsuario) {
        try {
            String body = authRestClient.get()
                    .uri("/api/biblioteca/usuario/id/{id}", idUsuario)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank() || !body.trim().startsWith("{")) {
                return null;
            }

            JsonNode usuario = objectMapper.readTree(body);
            String id = texto(usuario, "idUsuario");

            if (id == null) {
                return null;
            }

            return new UsuarioExterno(
                    id,
                    texto(usuario, "nombre"),
                    texto(usuario, "rol")
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return null;
            }

            throw new ServicioExternoNoDisponibleException(
                    "Auth respondio con error al validar el usuario", exception);
        } catch (RestClientException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo consultar el microservicio Auth", exception);
        } catch (RuntimeException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "Auth devolvio una respuesta invalida", exception);
        }
    }

    private String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }

        String texto = valor.asString();
        return texto == null || texto.isBlank() ? null : texto;
    }
}
