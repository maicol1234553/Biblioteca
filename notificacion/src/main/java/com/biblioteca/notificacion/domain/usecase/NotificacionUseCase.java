package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.LimiteReintentosExcedidoException;
import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.model.ContenidoNotificacion;
import com.biblioteca.notificacion.domain.model.EventoNotificacion;
import com.biblioteca.notificacion.domain.model.EstadoNotificacion;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.HtmlUtil;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.model.PlantillaRenderer;
import com.biblioteca.notificacion.domain.model.PoliticaNotificaciones;
import com.biblioteca.notificacion.domain.model.ResultadoEnvio;
import com.biblioteca.notificacion.domain.model.TipoNotificacion;
import com.biblioteca.notificacion.domain.model.UsuarioContacto;
import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class NotificacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificacionUseCase.class);

    private final NotificacionGateway notificacionGateway;
    private final HistorialEnvioGateway historialEnvioGateway;
    private final PlantillaNotificacionGateway plantillaGateway;
    private final AuthGateway authGateway;
    private final CorreoGateway correoGateway;
    private final PoliticaNotificaciones politica;
    private final Clock clock;

    public Notificacion enviarCorreo(String idUsuario,
                                     String destinatario,
                                     String asunto,
                                     String mensaje,
                                     String tipo) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        TipoNotificacion tipoEnum = TipoNotificacion.desde(tipo);
        Notificacion notificacion = nuevaNotificacion(idUsuario, destinatario, tipoEnum, ahora);
        String nombre = idUsuario;
        UsuarioContacto contacto = null;

        if (esBlanco(destinatario)) {
            RuntimeException errorAuth = null;

            try {
                contacto = authGateway.buscarUsuario(idUsuario);
            } catch (RuntimeException exception) {
                errorAuth = exception;
            }

            if (contacto == null || esBlanco(contacto.correo())) {
                completarContenido(
                        notificacion,
                        construirContenido(tipoEnum, asunto, mensaje, idUsuario, idUsuario, ahora)
                );
                notificacion = notificacionGateway.guardarNotificacion(notificacion);
                registrarFallo(
                        notificacion,
                        "No se pudo resolver el correo del usuario " + idUsuario,
                        ahora
                );

                if (errorAuth != null) {
                    throw errorAuth;
                }

                throw new DestinatarioInvalidoException(
                        "No se pudo resolver el correo del usuario " + idUsuario);
            }

            nombre = esBlanco(contacto.nombre()) ? idUsuario : contacto.nombre();
            notificacion.setDestinatario(contacto.correo());
        }

        completarContenido(
                notificacion,
                construirContenido(tipoEnum, asunto, mensaje, nombre, idUsuario, ahora)
        );
        notificacion = notificacionGateway.guardarNotificacion(notificacion);

        return intentarEnvio(notificacion, ahora);
    }

    public Notificacion enviarEvento(EventoNotificacion evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento de notificacion es obligatorio");
        }

        TipoNotificacion tipo = TipoNotificacion.desde(evento.tipo());
        String mensaje = evento.mensaje();

        if (!esBlanco(evento.referenciaId())) {
            String base = esBlanco(mensaje)
                    ? ContenidoNotificacion.mensajePorDefecto(tipo)
                    : mensaje;
            mensaje = base + "\nReferencia: " + evento.referenciaId();
        }

        return enviarCorreo(evento.idUsuario(), null, null, mensaje, tipo.name());
    }

    public Notificacion enviarPrestamoConfirmado(String idUsuario,
                                                 String idPrestamo,
                                                 String destinatario,
                                                 String mensaje) {
        String contenido = esBlanco(mensaje)
                ? "Tu prestamo con id " + idPrestamo + " fue confirmado correctamente."
                : mensaje;

        return enviarCorreo(
                idUsuario,
                destinatario,
                null,
                contenido,
                TipoNotificacion.PRESTAMO_CONFIRMADO.name()
        );
    }

    public Notificacion enviarLibroDisponible(String idUsuario,
                                              String idLibro,
                                              String destinatario,
                                              String mensaje) {
        String contenido = esBlanco(mensaje)
                ? "El libro con id " + idLibro + " que reservaste ya esta disponible."
                : mensaje;

        return enviarCorreo(
                idUsuario,
                destinatario,
                null,
                contenido,
                TipoNotificacion.LIBRO_DISPONIBLE.name()
        );
    }

    public Notificacion enviarAvisoVencido(String idUsuario,
                                           String idPrestamo,
                                           String destinatario,
                                           String mensaje) {
        String contenido = esBlanco(mensaje)
                ? "Tu prestamo con id " + idPrestamo + " se encuentra vencido."
                : mensaje;

        return enviarCorreo(
                idUsuario,
                destinatario,
                null,
                contenido,
                TipoNotificacion.PRESTAMO_VENCIDO.name()
        );
    }

    public Notificacion reintentarNotificacion(String idNotificacion) {
        Notificacion notificacion = buscarNotificacionInterna(idNotificacion);
        LocalDateTime ahora = LocalDateTime.now(clock);

        if (EstadoNotificacion.ENVIADA.name().equals(notificacion.getEstado())) {
            throw new EstadoInvalidoException("La notificacion ya fue enviada");
        }

        long intentos = historialEnvioGateway.contarPorNotificacion(idNotificacion);

        if (intentos >= politica.maxReintentos()) {
            throw new LimiteReintentosExcedidoException(
                    "La notificacion alcanzo el maximo de " + politica.maxReintentos()
                            + " intentos");
        }

        if (esBlanco(notificacion.getDestinatario())) {
            UsuarioContacto contacto;

            try {
                contacto = authGateway.buscarUsuario(notificacion.getIdUsuario());
            } catch (RuntimeException exception) {
                registrarFallo(notificacion, exception.getMessage(), ahora);
                throw exception;
            }

            if (contacto == null || esBlanco(contacto.correo())) {
                registrarFallo(
                        notificacion,
                        "No se pudo resolver el correo del usuario " + notificacion.getIdUsuario(),
                        ahora
                );
                throw new DestinatarioInvalidoException(
                        "No se pudo resolver el correo del usuario " + notificacion.getIdUsuario());
            }

            notificacion.setDestinatario(contacto.correo());
            notificacion = notificacionGateway.actualizarNotificacion(notificacion);
        }

        return intentarEnvio(notificacion, ahora);
    }

    public List<Notificacion> reintentarFallidas() {
        List<Notificacion> procesadas = new ArrayList<>();

        for (Notificacion notificacion : notificacionGateway.listarPorEstado(
                EstadoNotificacion.FALLIDA.name())) {
            long intentos = historialEnvioGateway.contarPorNotificacion(
                    notificacion.getIdNotificacion());

            if (intentos >= politica.maxReintentos()) {
                continue;
            }

            try {
                procesadas.add(reintentarNotificacion(notificacion.getIdNotificacion()));
            } catch (RuntimeException exception) {
                log.warn("No se pudo reintentar la notificacion {}: {}",
                        notificacion.getIdNotificacion(), exception.getMessage());
                procesadas.add(notificacion);
            }
        }

        return procesadas;
    }

    public List<Notificacion> consultarHistorialUsuario(String idUsuario) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        return notificacionGateway.listarPorUsuario(idUsuario);
    }

    public List<Notificacion> consultarPendientes() {
        return notificacionGateway.listarPorEstados(List.of(
                EstadoNotificacion.PENDIENTE.name(),
                EstadoNotificacion.FALLIDA.name()
        ));
    }

    public List<Notificacion> listarNotificaciones() {
        return notificacionGateway.listarNotificaciones();
    }

    public Notificacion buscarNotificacion(String idNotificacion) {
        return buscarNotificacionInterna(idNotificacion);
    }

    public List<HistorialEnvio> consultarHistorialEnvios(String idNotificacion) {
        buscarNotificacionInterna(idNotificacion);

        return historialEnvioGateway.listarPorNotificacion(idNotificacion);
    }

    public void eliminarNotificacion(String idNotificacion) {
        buscarNotificacionInterna(idNotificacion);
        historialEnvioGateway.eliminarPorNotificacion(idNotificacion);
        notificacionGateway.eliminarNotificacion(idNotificacion);
    }

    private Notificacion intentarEnvio(Notificacion notificacion, LocalDateTime ahora) {
        try {
            correoGateway.enviar(
                    notificacion.getDestinatario(),
                    notificacion.getAsunto(),
                    notificacion.getMensaje()
            );

            notificacion.setEstado(EstadoNotificacion.ENVIADA.name());
            notificacion.setFechaEnvio(ahora);
            Notificacion actualizada = notificacionGateway.actualizarNotificacion(notificacion);
            registrarHistorial(actualizada, ResultadoEnvio.EXITOSO, null, ahora);

            return actualizada;
        } catch (DestinatarioInvalidoException | EnvioCorreoException exception) {
            registrarFallo(notificacion, exception.getMessage(), ahora);
            throw exception;
        } catch (RuntimeException exception) {
            registrarFallo(notificacion, exception.getMessage(), ahora);
            throw new EnvioCorreoException("Error inesperado al enviar el correo", exception);
        }
    }

    private void registrarHistorial(Notificacion notificacion,
                                    ResultadoEnvio resultado,
                                    String mensajeError,
                                    LocalDateTime fecha) {
        HistorialEnvio historial = new HistorialEnvio();
        historial.setIdHistorial(UUID.randomUUID().toString());
        historial.setIdNotificacion(notificacion.getIdNotificacion());
        historial.setFechaIntento(fecha);
        historial.setResultado(resultado.name());
        historial.setMensajeError(mensajeError);

        historialEnvioGateway.guardarHistorial(historial);
    }

    private void registrarFallo(Notificacion notificacion,
                                String mensajeError,
                                LocalDateTime fecha) {
        notificacion.setEstado(EstadoNotificacion.FALLIDA.name());
        notificacionGateway.actualizarNotificacion(notificacion);
        registrarHistorial(notificacion, ResultadoEnvio.FALLIDO, mensajeError, fecha);
    }

    private Notificacion nuevaNotificacion(String idUsuario,
                                           String destinatario,
                                           TipoNotificacion tipo,
                                           LocalDateTime fecha) {
        String asunto = ContenidoNotificacion.asuntoPorDefecto(tipo);
        String mensaje = ContenidoNotificacion.html(
                asunto, ContenidoNotificacion.mensajePorDefecto(tipo));

        return new Notificacion(
                UUID.randomUUID().toString(),
                idUsuario,
                esBlanco(destinatario) ? null : destinatario,
                asunto,
                mensaje,
                tipo.name(),
                fecha,
                null,
                EstadoNotificacion.PENDIENTE.name()
        );
    }

    private void completarContenido(Notificacion notificacion, ContenidoFinal contenido) {
        notificacion.setAsunto(contenido.asunto());
        notificacion.setMensaje(contenido.html());
    }

    private ContenidoFinal construirContenido(TipoNotificacion tipo,
                                              String asunto,
                                              String mensaje,
                                              String nombre,
                                              String idUsuario,
                                              LocalDateTime fecha) {
        PlantillaNotificacion plantilla = plantillaGateway.buscarActivaPorTipo(tipo.name());
        String mensajeBase = esBlanco(mensaje)
                ? ContenidoNotificacion.mensajePorDefecto(tipo)
                : mensaje;

        if (plantilla != null) {
            Map<String, String> variables = new HashMap<>();
            variables.put("nombre", HtmlUtil.escape(nombre));
            variables.put("idUsuario", idUsuario == null ? "" : idUsuario);
            variables.put("mensaje", HtmlUtil.escape(mensajeBase));
            variables.put("fecha", fecha.toString());

            return new ContenidoFinal(
                    PlantillaRenderer.render(plantilla.getAsunto(), variables),
                    PlantillaRenderer.render(plantilla.getContenido(), variables)
            );
        }

        String asuntoFinal = esBlanco(asunto)
                ? ContenidoNotificacion.asuntoPorDefecto(tipo)
                : asunto;

        return new ContenidoFinal(
                asuntoFinal,
                ContenidoNotificacion.html(asuntoFinal, mensajeBase)
        );
    }

    private Notificacion buscarNotificacionInterna(String idNotificacion) {
        Notificacion notificacion = notificacionGateway.buscarNotificacionPorId(idNotificacion);

        if (notificacion == null) {
            throw new NotificacionNoEncontradaException(
                    "Notificacion no encontrada con id: " + idNotificacion);
        }

        return notificacion;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private record ContenidoFinal(String asunto, String html) {
    }
}
