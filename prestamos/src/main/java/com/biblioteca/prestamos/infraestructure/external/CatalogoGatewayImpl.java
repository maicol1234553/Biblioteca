package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;
import com.biblioteca.prestamos.domain.model.gateway.CatalogoGateway;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class CatalogoGatewayImpl implements CatalogoGateway {

    private final RestClient catalogoRestClient;
    private final ObjectMapper objectMapper;
    private final PrestamosProperties properties;

    public CatalogoGatewayImpl(@Qualifier("catalogoRestClient") RestClient catalogoRestClient,
                               ObjectMapper objectMapper,
                               PrestamosProperties properties) {
        this.catalogoRestClient = catalogoRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public EjemplarExterno buscarEjemplar(String idEjemplar) {
        try {
            String body = catalogoRestClient.get()
                    .uri("/api/biblioteca/ejemplares/{id}", idEjemplar)
                    .retrieve()
                    .body(String.class);

            JsonNode ejemplar = parsearObjeto(body);

            if (ejemplar == null) {
                return null;
            }

            String id = texto(ejemplar, "idEjemplar");

            if (id == null) {
                return null;
            }

            return new EjemplarExterno(
                    id,
                    texto(ejemplar, "idLibro"),
                    texto(ejemplar, "tituloLibro"),
                    texto(ejemplar, "codigoInventario"),
                    texto(ejemplar, "ubicacion"),
                    texto(ejemplar, "estado")
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return null;
            }

            throw new ServicioExternoNoDisponibleException(
                    "Catalogo respondio con error al consultar el ejemplar", exception);
        } catch (RestClientException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo consultar el microservicio Catalogo", exception);
        } catch (RuntimeException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "Catalogo devolvio una respuesta invalida", exception);
        }
    }

    @Override
    public DisponibilidadLibroExterna consultarDisponibilidadLibro(String idLibro) {
        try {
            String body = catalogoRestClient.get()
                    .uri("/api/biblioteca/libros/{id}/disponibilidad", idLibro)
                    .retrieve()
                    .body(String.class);

            JsonNode disponibilidad = parsearObjeto(body);

            if (disponibilidad == null) {
                return null;
            }

            String id = texto(disponibilidad, "idLibro");

            if (id == null) {
                return null;
            }

            return new DisponibilidadLibroExterna(
                    id,
                    texto(disponibilidad, "titulo"),
                    disponibilidad.path("totalEjemplares").asInt(0),
                    disponibilidad.path("disponibles").asInt(0),
                    disponibilidad.path("disponible").asBoolean(false)
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return null;
            }

            throw new ServicioExternoNoDisponibleException(
                    "Catalogo respondio con error al consultar la disponibilidad", exception);
        } catch (RestClientException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo consultar el microservicio Catalogo", exception);
        } catch (RuntimeException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "Catalogo devolvio una respuesta invalida", exception);
        }
    }

    @Override
    public void actualizarEstadoEjemplar(String idEjemplar, String estado) {
        String adminUsuarioId = properties.servicios().catalogoAdminUsuarioId();

        if (adminUsuarioId == null || adminUsuarioId.isBlank()) {
            throw new ServicioExternoNoDisponibleException(
                    "Configure prestamos.servicios.catalogo-admin-usuario-id con un usuario "
                            + "ADMINISTRADOR para actualizar el estado de los ejemplares");
        }

        try {
            catalogoRestClient.put()
                    .uri("/api/biblioteca/ejemplares/{id}", idEjemplar)
                    .header("X-Usuario-Id", adminUsuarioId.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new ServicioExternoNoDisponibleException(
                    "No se pudo actualizar el estado del ejemplar en Catalogo", exception);
        }
    }

    private JsonNode parsearObjeto(String body) {
        if (body == null || body.isBlank() || !body.trim().startsWith("{")) {
            return null;
        }

        JsonNode nodo = objectMapper.readTree(body);
        return nodo.isObject() ? nodo : null;
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
