package com.biblioteca.auth.infraestructure.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class NotificationGatewayImplTest {

    private static final String BASE_URL = "http://localhost:8084";
    private static final String URL_ENVIO = BASE_URL + "/notificaciones/enviar-correo";
    private static final String ID_USUARIO = "8e03853d-7058-40a4-9692-2b5fbf82d47e";

    private MockRestServiceServer server;
    private NotificationGatewayImpl notificationGateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        notificationGateway = new NotificationGatewayImpl(builder.build());
    }

    @Test
    void enviarCorreo_enviaJsonAlServicioDeNotificacion() {
        server.expect(requestTo(URL_ENVIO))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.idUsuario").value(ID_USUARIO))
                .andExpect(jsonPath("$.destinatario").value("persona@biblioteca.com"))
                .andExpect(jsonPath("$.asunto").value("Confirmación"))
                .andExpect(jsonPath("$.mensaje").value("Tu préstamo fue confirmado"))
                .andExpect(jsonPath("$.tipo").value("PRESTAMO_CONFIRMADO"))
                .andRespond(withStatus(HttpStatus.OK));

        notificationGateway.enviarCorreo(
                ID_USUARIO,
                "persona@biblioteca.com",
                "Confirmación",
                "Tu préstamo fue confirmado",
                "PRESTAMO_CONFIRMADO"
        );

        server.verify();
    }

    @Test
    void enviarCorreo_sinDestinatario_noLlamaAlServicio() {
        assertDoesNotThrow(() -> notificationGateway.enviarCorreo(
                ID_USUARIO,
                " ",
                "Asunto",
                "Mensaje",
                "GENERICA"
        ));



        server.verify();
    }

    @Test
    void enviarCorreo_siElServicioRespondeError_noPropagaExcepcion() {
        for (int intento = 0; intento < 3; intento++) {
            server.expect(requestTo(URL_ENVIO))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"mensaje\":\"fallo interno\"}"));
        }

        assertDoesNotThrow(() -> notificationGateway.enviarCorreo(
                ID_USUARIO,
                "persona@biblioteca.com",
                "Confirmación",
                "Tu préstamo fue confirmado",
                "PRESTAMO_CONFIRMADO"
        ));

        server.verify();
    }
}
