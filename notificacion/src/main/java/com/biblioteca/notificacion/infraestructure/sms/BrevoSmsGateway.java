package com.biblioteca.notificacion.infraestructure.sms;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioSmsException;
import com.biblioteca.notificacion.domain.model.gateway.SmsGateway;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de SMS sobre la API de Brevo (ex Sendinblue):
 * POST https://api.brevo.com/v3/sms/
 *
 * La api-key llega por variable de entorno (BREVO_API_KEY) y nunca se
 * persiste ni se registra en log.
 */
@Component
public class BrevoSmsGateway implements SmsGateway {

    private static final String RUTA_SMS = "/v3/sms/";

    private final RestClient brevoRestClient;
    private final NotificacionProperties properties;
    private final ObjectMapper objectMapper;

    public BrevoSmsGateway(@Qualifier("brevoRestClient") RestClient brevoRestClient,
                           NotificacionProperties properties,
                           ObjectMapper objectMapper) {
        this.brevoRestClient = brevoRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void enviar(String telefono, String mensaje) {
        validar(telefono, mensaje);
        validarConfiguracion();

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("sender", properties.sms().sender());
        cuerpo.put("recipients", List.of(Map.of("phoneNumbers", List.of(telefono))));
        cuerpo.put("content", mensaje);

        String respuesta;

        try {
            respuesta = brevoRestClient.post()
                    .uri(RUTA_SMS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(escribirJson(cuerpo))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            throw new EnvioSmsException(
                    "Brevo rechazo el SMS (" + exception.getStatusCode().value() + "): "
                            + detalleError(exception.getResponseBodyAsString()),
                    exception);
        } catch (RestClientException exception) {
            throw new EnvioSmsException(
                    "No se pudo contactar al proveedor de SMS", exception);
        }

        confirmarAceptacion(respuesta);
    }

    private void validar(String telefono, String mensaje) {
        if (telefono == null || telefono.isBlank()) {
            throw new DestinatarioInvalidoException("El telefono es obligatorio");
        }

        if (!telefono.matches("\\+[1-9]\\d{6,14}")) {
            throw new DestinatarioInvalidoException(
                    "El telefono no esta en formato internacional E.164: " + telefono);
        }

        if (mensaje == null || mensaje.isBlank()) {
            throw new DestinatarioInvalidoException("El mensaje del SMS esta vacio");
        }
    }

    private void validarConfiguracion() {
        NotificacionProperties.Sms sms = properties.sms();

        if (sms == null || sms.apiKey() == null || sms.apiKey().isBlank()) {
            throw new EnvioSmsException(
                    "SMS no configurado: falta la variable de entorno BREVO_API_KEY");
        }

        if (sms.sender() == null || sms.sender().isBlank()) {
            throw new EnvioSmsException(
                    "SMS no configurado: falta la variable de entorno SMS_SENDER");
        }
    }

    private void confirmarAceptacion(String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            return;
        }

        try {
            JsonNode nodo = objectMapper.readTree(respuesta);
            JsonNode codigo = nodo.path("code");

            if (!codigo.isMissingNode()
                    && !codigo.isNull()
                    && !"request-accepted".equals(codigo.asString())) {
                throw new EnvioSmsException(
                        "Brevo no acepto el SMS: " + nodo.path("message").asString());
            }
        } catch (EnvioSmsException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            // Un cuerpo inesperado no debe tumbar el envio ya aceptado por Brevo.
        }
    }

    private String escribirJson(Object cuerpo) {
        try {
            return objectMapper.writeValueAsString(cuerpo);
        } catch (RuntimeException exception) {
            throw new EnvioSmsException("No se pudo construir la peticion SMS", exception);
        }
    }

    private String detalleError(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return "sin detalle";
        }

        try {
            JsonNode nodo = objectMapper.readTree(cuerpo);
            JsonNode mensaje = nodo.path("message");

            if (!mensaje.isMissingNode() && !mensaje.isNull()) {
                return mensaje.asString();
            }
        } catch (RuntimeException exception) {
            // Se devuelve el cuerpo crudo.
        }

        return cuerpo.length() > 300 ? cuerpo.substring(0, 300) : cuerpo;
    }
}
