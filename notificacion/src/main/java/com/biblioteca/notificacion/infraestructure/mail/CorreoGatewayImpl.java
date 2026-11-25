package com.biblioteca.notificacion.infraestructure.mail;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.infraestructure.config.NotificacionProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CorreoGatewayImpl implements CorreoGateway {

    private final JavaMailSender mailSender;
    private final NotificacionProperties properties;

    @Override
    public void enviar(String destinatario, String asunto, String contenidoHtml) {
        validarDestinatario(destinatario);

        String remitente = properties.remitente() == null || properties.remitente().isBlank()
                ? "no-reply@biblioteca.local"
                : properties.remitente();

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto == null ? "" : asunto);
            helper.setText(contenidoHtml == null ? "" : contenidoHtml, true);

            mailSender.send(mensaje);
        } catch (MessagingException exception) {
            throw new EnvioCorreoException("No se pudo construir el correo", exception);
        } catch (MailException exception) {
            throw new EnvioCorreoException("No se pudo enviar el correo por SMTP", exception);
        }
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
