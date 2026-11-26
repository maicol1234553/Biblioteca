package com.biblioteca.notificacion.infraestructure.mail;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;








@Component
public class CorreoGatewayImpl implements CorreoGateway {

    private static final Logger log = LoggerFactory.getLogger(CorreoGatewayImpl.class);
    private static final String SENDGRID_URL_POR_DEFECTO = "https://api.sendgrid.com/v3/mail/send";
    private static final String BREVO_URL_POR_DEFECTO = "https://api.brevo.com/v3/smtp/email";

    private final JavaMailSender mailSender;
    private final NotificacionProperties properties;
    private final RestClient httpClient;

    public CorreoGatewayImpl(JavaMailSender mailSender, NotificacionProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(90));

        this.httpClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public void enviar(String destinatario, String asunto, String contenidoHtml) {
        validarDestinatario(destinatario);

        String remitente = remitente();
        String asuntoSeguro = asunto == null ? "" : asunto;
        String contenido = contenidoHtml == null ? "" : contenidoHtml;
        NotificacionProperties.CorreoApi correoApi = properties.correoApi();

        if (tieneTexto(correoApi == null ? null : correoApi.sendgridApiKey())) {
            log.info("Proveedor de correo seleccionado: SENDGRID");
            enviarSendgrid(correoApi.sendgridApiKey(), remitente, destinatario, asuntoSeguro, contenido);
            return;
        }

        if (tieneTexto(correoApi == null ? null : correoApi.brevoApiKey())) {
            log.info("Proveedor de correo seleccionado: BREVO");
            enviarBrevo(correoApi.brevoApiKey(), remitente, destinatario, asuntoSeguro, contenido);
            return;
        }

        log.info("Proveedor de correo seleccionado: SMTP");
        enviarSmtp(remitente, destinatario, asuntoSeguro, contenido);
    }

    private void enviarSendgrid(String apiKey,
                                String remitente,
                                String destinatario,
                                String asunto,
                                String contenidoHtml) {
        String url = valorODefecto(
                properties.correoApi() == null ? null : properties.correoApi().sendgridUrl(),
                SENDGRID_URL_POR_DEFECTO);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("personalizations", List.of(Map.of(
                "to", List.of(Map.of("email", destinatario))
        )));
        body.put("from", Map.of("email", remitente));
        body.put("subject", asunto);
        body.put("content", List.of(Map.of(
                "type", "text/html",
                "value", contenidoHtml
        )));

        try {
            ResponseEntity<String> response = httpClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);

            log.info("SendGrid respondio HTTP {}: {}",
                    response.getStatusCode().value(), resumen(response.getBody()));
        } catch (RestClientResponseException exception) {
            log.warn("SendGrid rechazo el correo HTTP {}: {}",
                    exception.getStatusCode().value(), resumen(exception.getResponseBodyAsString()));
            throw new EnvioCorreoException(
                    "SendGrid rechazo el correo (" + exception.getStatusCode().value()
                            + "): " + exception.getResponseBodyAsString(), exception);
        } catch (RestClientException exception) {
            log.warn("No se pudo enviar el correo por SendGrid: {}", exception.getMessage());
            throw new EnvioCorreoException("No se pudo enviar el correo por SendGrid", exception);
        }
    }

    private void enviarBrevo(String apiKey,
                             String remitente,
                             String destinatario,
                             String asunto,
                             String contenidoHtml) {
        String url = valorODefecto(
                properties.correoApi() == null ? null : properties.correoApi().brevoUrl(),
                BREVO_URL_POR_DEFECTO);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sender", Map.of("email", remitente));
        body.put("to", List.of(Map.of("email", destinatario)));
        body.put("subject", asunto);
        body.put("htmlContent", contenidoHtml);

        try {
            ResponseEntity<String> response = httpClient.post()
                    .uri(url)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);

            log.info("Brevo respondio HTTP {}: {}",
                    response.getStatusCode().value(), resumen(response.getBody()));
        } catch (RestClientResponseException exception) {
            log.warn("Brevo rechazo el correo HTTP {}: {}",
                    exception.getStatusCode().value(), resumen(exception.getResponseBodyAsString()));
            throw new EnvioCorreoException(
                    "Brevo rechazo el correo (" + exception.getStatusCode().value()
                            + "): " + exception.getResponseBodyAsString(), exception);
        } catch (RestClientException exception) {
            log.warn("No se pudo enviar el correo por Brevo: {}", exception.getMessage());
            throw new EnvioCorreoException("No se pudo enviar el correo por Brevo", exception);
        }
    }

    private void enviarSmtp(String remitente,
                            String destinatario,
                            String asunto,
                            String contenidoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(mensaje);
            log.info("Correo enviado por SMTP a {}", destinatario);
        } catch (MessagingException exception) {
            throw new EnvioCorreoException("No se pudo construir el correo", exception);
        } catch (MailException exception) {
            throw new EnvioCorreoException("No se pudo enviar el correo por SMTP", exception);
        }
    }

    private String remitente() {
        return properties.remitente() == null || properties.remitente().isBlank()
                ? "no-reply@biblioteca.local"
                : properties.remitente();
    }

    private String valorODefecto(String valor, String defecto) {
        return tieneTexto(valor) ? valor.trim() : defecto;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String resumen(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replaceAll("\\s+", " ").trim();
        return limpio.length() <= 500 ? limpio : limpio.substring(0, 500) + "...";
    }

    private void validarDestinatario(String destinatario) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new DestinatarioInvalidoException("El destinatario es obligatorio");
        }

        try {
            InternetAddress direccion = new InternetAddress(destinatario, true);
            direccion.validate();
        } catch (AddressException exception) {
            throw new DestinatarioInvalidoException(
                    "El destinatario no es un correo valido: " + destinatario, exception);
        }
    }
}
