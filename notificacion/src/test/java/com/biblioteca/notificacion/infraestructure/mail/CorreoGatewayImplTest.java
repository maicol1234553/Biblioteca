package com.biblioteca.notificacion.infraestructure.mail;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import jakarta.mail.BodyPart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorreoGatewayImplTest {

    @Mock
    private JavaMailSender mailSender;

    private CorreoGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        NotificacionProperties properties = new NotificacionProperties(
                "no-reply@biblioteca.local",
                new NotificacionProperties.Reintentos(3, 2000),
                new NotificacionProperties.Job(60000),
                new NotificacionProperties.Recordatorio(1),
                new NotificacionProperties.Servicios("http://localhost:8081", 5),
                new NotificacionProperties.Sms("test-key", "Biblioteca", "57", 10),
                "CORREO"
        );
        gateway = new CorreoGatewayImpl(mailSender, properties);
    }

    @Test
    void enviaCorreoHtmlConRemitenteYDestinatario() throws Exception {
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        gateway.enviar("user@example.com", "Asunto de prueba", "<h1>Hola</h1>");

        verify(mailSender).send(mensaje);
        assertEquals("Asunto de prueba", mensaje.getSubject());
        assertEquals("user@example.com", mensaje.getAllRecipients()[0].toString());
        assertEquals("no-reply@biblioteca.local", mensaje.getFrom()[0].toString());

        mensaje.saveChanges();
        Object contenido = mensaje.getContent();
        assertTrue(contenido != null);

        java.io.ByteArrayOutputStream salida = new java.io.ByteArrayOutputStream();
        mensaje.writeTo(salida);
        String mensajeSerializado = salida.toString(java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(mensajeSerializado.contains("<h1>Hola</h1>"));
        assertTrue(mensajeSerializado.toLowerCase().contains("text/html"));
    }

    @Test
    void rechazaDestinatarioInvalidoSinEnviar() {
        assertThrows(DestinatarioInvalidoException.class,
                () -> gateway.enviar("correo-invalido", "Asunto", "<p>Mensaje</p>"));

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void envuelveErroresSmtp() {
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mensaje);
        doThrow(new MailSendException("smtp caido"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThrows(EnvioCorreoException.class,
                () -> gateway.enviar("user@example.com", "Asunto", "<p>Mensaje</p>"));
    }
}
