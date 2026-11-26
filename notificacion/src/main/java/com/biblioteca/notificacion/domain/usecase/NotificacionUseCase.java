package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.exception.EnvioSmsException;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.LimiteReintentosExcedidoException;
import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.model.CanalNotificacion;
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
import com.biblioteca.notificacion.domain.model.Telefono;
import com.biblioteca.notificacion.domain.model.TipoNotificacion;
import com.biblioteca.notificacion.domain.model.UsuarioContacto;
import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.SmsGateway;
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
    private final SmsGateway smsGateway;
    private final PoliticaNotificaciones politica;
    private final Clock clock;

    /**
     * Envia por el canal configurado como por defecto (SMS en produccion).
     * Es el punto de entrada de los eventos de otros microservicios y de los
     * recordatorios.
     */
    public Notificacion enviar(String idUsuario,
                              String destinatario,
                              String asunto,
                              String mensaje,
                              String tipo) {
        return enviar(idUsuario, destinatario, asunto, mensaje, tipo,
                politica.canalPorDefecto());
    }

    public Notificacion enviarCorreo(String idUsuario,
                                     String destinatario,
                                     String asunto,
                                     String mensaje,
                                     String tipo) {
        return enviar(idUsuario, destinatario, asunto, mensaje, tipo,
                CanalNotificacion.CORREO);
    }

    public Notificacion enviarSms(String idUsuario,
                                   String destinatario,
                                   String mensaje,
                                   String tipo) {
        return enviar(idUsuario, destinatario, null, mensaje, tipo, CanalNotificacion.SMS);
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

        return enviar(evento.idUsuario(), null, null, mensaje, tipo.name());
    }

    public Notificacion enviarPrestamoConfirmado(String idUsuario,
                                                 String idPrestamo,
                                                 String destinatario,
                                                 String mensaje) {
        String contenido = esBlanco(mensaje)
                ? "Tu prestamo con id " + idPrestamo + " fue confirmado correctamente."
                : mensaje;

        return enviar(
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

        return enviar(
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

        return enviar(
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

            String canalGuardado = notificacion.getCanal();
            CanalNotificacion canal = CanalNotificacion.desdeGuardado(canalGuardado);
            String destino = destinoParaCanal(contacto, canal);

            if (destino == null && canal == CanalNotificacion.SMS) {
                // Respaldo: sin telefono reintentamos por correo.
                destino = contacto == null ? null
                        : (correoUsable(contacto.correo()) ? contacto.correo() : null);
                canal = CanalNotificacion.CORREO;
                notificacion.setCanal(canal.name());
            }

            if (destino == null) {
                registrarFallo(
                        notificacion,
                        "No se pudo resolver el destinatario del usuario "
                                + notificacion.getIdUsuario(),
                        ahora
                );
                throw new DestinatarioInvalidoException(
                        "No se pudo resolver el destinatario del usuario "
                                + notificacion.getIdUsuario());
            }

            notificacion.setDestinatario(destino);
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

    private Notificacion enviar(String idUsuario,
                                String destinatario,
                                String asunto,
                                String mensaje,
                                String tipo,
                                CanalNotificacion canalSolicitado) {
        if (esBlanco(idUsuario)) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        CanalNotificacion canal = canalSolicitado == null
                ? politica.canalPorDefecto()
                : canalSolicitado;
        LocalDateTime ahora = LocalDateTime.now(clock);
        TipoNotificacion tipoEnum = TipoNotificacion.desde(tipo);
        Notificacion notificacion = nuevaNotificacion(
                idUsuario, destinatario, tipoEnum, ahora, canal);

        String nombre = idUsuario;
        UsuarioContacto contacto = null;

        try {
            if (canal == CanalNotificacion.SMS) {
                String telefono = Telefono.normalizar(destinatario, politica.codigoPais());

                if (telefono == null) {
                    contacto = authGateway.buscarUsuario(idUsuario);
                    telefono = destinoParaCanal(contacto, CanalNotificacion.SMS);
                }

                if (telefono != null) {
                    notificacion.setDestinatario(telefono);
                } else {
                    // Respaldo: sin telefono utilizable se intenta por correo.
                    String correo = correoUsable(destinatario) ? destinatario.trim() : null;

                    if (correo == null && contacto != null) {
                        correo = contacto.correo();
                    }

                    if (correo == null) {
                        throw new DestinatarioInvalidoException(
                                "El usuario " + idUsuario
                                        + " no tiene telefono ni correo registrados");
                    }

                    canal = CanalNotificacion.CORREO;
                    notificacion.setCanal(canal.name());
                    notificacion.setDestinatario(correo);

                    if (contacto != null && !esBlanco(contacto.nombre())) {
                        nombre = contacto.nombre();
                    }

                    log.warn("Sin telefono para el usuario {}; se enviara por correo",
                            idUsuario);
                }
            } else if (esBlanco(destinatario)) {
                contacto = authGateway.buscarUsuario(idUsuario);

                if (contacto == null || esBlanco(contacto.correo())) {
                    throw new DestinatarioInvalidoException(
                            "No se pudo resolver el correo del usuario " + idUsuario);
                }

                nombre = esBlanco(contacto.nombre()) ? idUsuario : contacto.nombre();
                notificacion.setDestinatario(contacto.correo());
            }
        } catch (RuntimeException exception) {
            completarContenido(notificacion, construirContenido(
                    tipoEnum, asunto, mensaje, nombre, idUsuario, ahora, canal));
            notificacion = notificacionGateway.guardarNotificacion(notificacion);
            registrarFallo(notificacion, exception.getMessage(), ahora);

            throw exception;
        }

        completarContenido(
                notificacion,
                construirContenido(tipoEnum, asunto, mensaje, nombre, idUsuario, ahora, canal)
        );
        notificacion = notificacionGateway.guardarNotificacion(notificacion);

        return intentarEnvio(notificacion, ahora);
    }

    private Notificacion intentarEnvio(Notificacion notificacion, LocalDateTime ahora) {
        CanalNotificacion canal = CanalNotificacion.desdeGuardado(notificacion.getCanal());

        try {
            if (canal == CanalNotificacion.SMS) {
                smsGateway.enviar(
                        notificacion.getDestinatario(),
                        notificacion.getMensaje()
                );
            } else {
                correoGateway.enviar(
                        notificacion.getDestinatario(),
                        notificacion.getAsunto(),
                        notificacion.getMensaje()
                );
            }

            notificacion.setEstado(EstadoNotificacion.ENVIADA.name());
            notificacion.setFechaEnvio(ahora);
            Notificacion actualizada = notificacionGateway.actualizarNotificacion(notificacion);
            registrarHistorial(actualizada, ResultadoEnvio.EXITOSO, null, ahora);

            return actualizada;
        } catch (DestinatarioInvalidoException | EnvioCorreoException | EnvioSmsException exception) {
            registrarFallo(notificacion, exception.getMessage(), ahora);
            throw exception;
        } catch (RuntimeException exception) {
            registrarFallo(notificacion, exception.getMessage(), ahora);
            throw canal == CanalNotificacion.SMS
                    ? new EnvioSmsException("Error inesperado al enviar el SMS", exception)
                    : new EnvioCorreoException("Error inesperado al enviar el correo", exception);
        }
    }

    /**
     * Destinatario canonico para el canal pedido: E.164 para SMS y correo
     * electronico para CORREO. Devuelve null cuando el contacto no permite
     * enviar por ese canal.
     */
    private String destinoParaCanal(UsuarioContacto contacto, CanalNotificacion canal) {
        if (contacto == null) {
            return null;
        }

        return canal == CanalNotificacion.SMS
                ? Telefono.normalizar(contacto.telefono(), politica.codigoPais())
                : (correoUsable(contacto.correo()) ? contacto.correo() : null);
    }

    private boolean correoUsable(String correo) {
        if (esBlanco(correo)) {
            return false;
        }

        String limpio = correo.trim();
        int arroba = limpio.indexOf('@');

        return arroba > 0 && arroba < limpio.length() - 1 && !limpio.contains(" ");
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
                                           LocalDateTime fecha,
                                           CanalNotificacion canal) {
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
                EstadoNotificacion.PENDIENTE.name(),
                canal.name()
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
                                              LocalDateTime fecha,
                                              CanalNotificacion canal) {
        PlantillaNotificacion plantilla = plantillaGateway.buscarActivaPorTipo(tipo.name());
        String mensajeBase = esBlanco(mensaje)
                ? ContenidoNotificacion.mensajePorDefecto(tipo)
                : mensaje;

        Map<String, String> variables = new HashMap<>();
        variables.put("nombre", HtmlUtil.escape(nombre));
        variables.put("idUsuario", idUsuario == null ? "" : idUsuario);
        variables.put("mensaje", HtmlUtil.escape(mensajeBase));
        variables.put("fecha", fecha.toString());

        String asuntoFinal = plantilla != null
                ? PlantillaRenderer.render(plantilla.getAsunto(), variables)
                : (esBlanco(asunto)
                        ? ContenidoNotificacion.asuntoPorDefecto(tipo)
                        : asunto);

        if (canal == CanalNotificacion.SMS) {
            // El cuerpo del SMS es texto plano: nunca se envia HTML por Brevo.
            Map<String, String> texto = new HashMap<>();
            texto.put("nombre", nombre == null ? "" : nombre);
            texto.put("idUsuario", idUsuario == null ? "" : idUsuario);
            texto.put("fecha", fecha.toString());

            return new ContenidoFinal(asuntoFinal, PlantillaRenderer.render(mensajeBase, texto));
        }

        return new ContenidoFinal(
                asuntoFinal,
                plantilla != null
                        ? PlantillaRenderer.render(plantilla.getContenido(), variables)
                        : ContenidoNotificacion.html(asuntoFinal, mensajeBase)
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
