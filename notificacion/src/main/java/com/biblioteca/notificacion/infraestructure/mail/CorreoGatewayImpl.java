package com.biblioteca.notificacion.infraestructure.mail;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

/**
 * Envia correos por SMTP o por API HTTP.
 *
 * Render Free bloquea los puertos SMTP (25/465/587), por lo que en produccion
 * se debe configurar SENDGRID_API_KEY o BREVO_API_KEY. Si no hay ninguna de
 * las dos, se usa SMTP (util para MailHog local o planes pagos).
 */
@Component
public class CorreoGatewayImpl implements CorreoGateway {

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
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

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
            enviarSendgrid(correoApi.sendgridApiKey(), remitente, destinatario, asuntoSeguro, contenido);
            return;
        }

        if (tieneTexto(correoApi == null ? null : correoApi.brevoApiKey())) {
            enviarBrevo(correoApi.brevoApiKey(), remitente, destinatario, asuntoSeguro, contenido);
            return;
        }

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
            httpClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new EnvioCorreoException(
                    "SendGrid rechazo el correo (" + exception.getStatusCode().value()
                            + "): " + exception.getResponseBodyAsString(), exception);
        } catch (RestClientException exception) {
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
            httpClient.post()
                    .uri(url)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new EnvioCorreoException(
                    "Brevo rechazo el correo (" + exception.getStatusCode().value()
                            + "): " + exception.getResponseBodyAsString(), exception);
        } catch (RestClientException exception) {
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
