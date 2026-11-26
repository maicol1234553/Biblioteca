package com.biblioteca.notificacion.infraestructure.mail;

import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CorreoGatewayHttpTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger llamadas = new AtomicInteger();
    private final AtomicReference<String> autorizacion = new AtomicReference<>();
    private final AtomicReference<String> cuerpo = new AtomicReference<>();
    private volatile int codigoRespuesta = 202;

    @BeforeEach
    void iniciarServidor() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/sendgrid", exchange -> {
            llamadas.incrementAndGet();
            autorizacion.set(exchange.getRequestHeaders().getFirst("Authorization"));
            cuerpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] respuesta = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(codigoRespuesta, respuesta.length);
            exchange.getResponseBody().write(respuesta);
            exchange.close();
        });
        server.createContext("/brevo", exchange -> {
            llamadas.incrementAndGet();
            autorizacion.set(exchange.getRequestHeaders().getFirst("api-key"));
            cuerpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] respuesta = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(codigoRespuesta, respuesta.length);
            exchange.getResponseBody().write(respuesta);
            exchange.close();
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void detenerServidor() {
        server.stop(0);
    }

    private CorreoGatewayImpl gateway(String sendgridKey,
                                      String brevoKey) {
        NotificacionProperties properties = new NotificacionProperties(
                " ",
                new NotificacionProperties.Reintentos(3, 2000),
                new NotificacionProperties.Job(60000),
                new NotificacionProperties.Recordatorio(1),
                new NotificacionProperties.Servicios("http://localhost:8081", 5),
                new NotificacionProperties.CorreoApi(
                        sendgridKey,
                        baseUrl + "/sendgrid",
                        brevoKey,
                        baseUrl + "/brevo"
                )
        );
        return new CorreoGatewayImpl(mock(JavaMailSender.class), properties);
    }

    @Test
    void enviaPorSendgridCuandoHayApiKey() {
        gateway("SG.clave", null).enviar("user@example.com", "Asunto", "<p>Hola</p>");

        assertEquals(1, llamadas.get());
        assertEquals("Bearer SG.clave", autorizacion.get());
        assertTrue(cuerpo.get().contains("user@example.com"));
        assertTrue(cuerpo.get().contains("<p>Hola</p>"));
        assertTrue(cuerpo.get().contains("no-reply@biblioteca.local"));
    }

    @Test
    void enviaPorBrevoCuandoNoHaySendgrid() {
        gateway(null, "xkeysib-clave").enviar("user@example.com", null, null);

        assertEquals(1, llamadas.get());
        assertEquals("xkeysib-clave", autorizacion.get());
        assertTrue(cuerpo.get().contains("user@example.com"));
    }

    @Test
    void envuelveErroresHttpDeSendgrid() {
        codigoRespuesta = 500;

        assertThrows(EnvioCorreoException.class,
                () -> gateway("SG.clave", null).enviar("user@example.com", "Asunto", "<p>Hola</p>"));
    }

    @Test
    void envuelveErroresHttpDeBrevo() {
        codigoRespuesta = 500;

        assertThrows(EnvioCorreoException.class,
                () -> gateway(null, "xkeysib-clave").enviar("user@example.com", "Asunto", "<p>Hola</p>"));
    }
}
