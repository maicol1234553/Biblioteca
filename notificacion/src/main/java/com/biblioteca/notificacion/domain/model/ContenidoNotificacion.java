package com.biblioteca.notificacion.domain.model;

public final class ContenidoNotificacion {

    private ContenidoNotificacion() {
    }

    public static String asuntoPorDefecto(TipoNotificacion tipo) {
        return switch (tipo) {
            case PRESTAMO_CONFIRMADO, PRESTAMO_REGISTRADO -> "Confirmacion de prestamo";
            case DEVOLUCION_PROXIMA -> "Recordatorio de devolucion";
            case DEVOLUCION_REGISTRADA -> "Devolucion registrada";
            case PRESTAMO_VENCIDO -> "Prestamo vencido";
            case LIBRO_DISPONIBLE -> "Libro disponible";
            case RESERVA_REGISTRADA -> "Reserva registrada";
            case RESERVA_EXPIRADA -> "Reserva expirada";
            case RENOVACION_REGISTRADA -> "Renovacion registrada";
            case MULTA_GENERADA -> "Multa generada";
            case RECUPERACION_PASSWORD -> "Recuperacion de contrasena";
            case GENERICA -> "Notificacion de la biblioteca";
        };
    }

    public static String mensajePorDefecto(TipoNotificacion tipo) {
        return switch (tipo) {
            case PRESTAMO_CONFIRMADO, PRESTAMO_REGISTRADO ->
                    "Tu prestamo fue registrado correctamente.";
            case DEVOLUCION_PROXIMA ->
                    "Te recordamos que la fecha de devolucion de tu prestamo esta proxima.";
            case DEVOLUCION_REGISTRADA ->
                    "La devolucion de tu prestamo fue registrada correctamente.";
            case PRESTAMO_VENCIDO ->
                    "Tu prestamo se encuentra vencido. Por favor realiza la devolucion.";
            case LIBRO_DISPONIBLE ->
                    "El libro que reservaste ya se encuentra disponible.";
            case RESERVA_REGISTRADA ->
                    "Tu reserva fue registrada correctamente.";
            case RESERVA_EXPIRADA ->
                    "Tu reserva expiro. Puedes realizar una nueva reserva.";
            case RENOVACION_REGISTRADA ->
                    "Tu prestamo fue renovado correctamente.";
            case MULTA_GENERADA ->
                    "Se genero una multa asociada a tu prestamo.";
            case RECUPERACION_PASSWORD ->
                    "Recibimos una solicitud para restablecer tu contrasena.";
            case GENERICA ->
                    "Tienes una nueva notificacion del Sistema de Biblioteca.";
        };
    }

    public static String html(String titulo, String mensaje) {
        String tituloSeguro = HtmlUtil.escape(titulo);
        String mensajeSeguro = HtmlUtil.escape(mensaje).replace("\n", "<br>");

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,sans-serif;color:#222;">
                    <div style="max-width:600px;margin:0 auto;padding:24px;">
                        <div style="background-color:#1f4e79;color:#ffffff;padding:16px 24px;border-radius:8px 8px 0 0;">
                            <h1 style="margin:0;font-size:20px;">Sistema de Biblioteca</h1>
                        </div>
                        <div style="background-color:#ffffff;padding:24px;border:1px solid #e1e4e8;">
                            <h2 style="margin-top:0;color:#1f4e79;font-size:18px;">%s</h2>
                            <p style="line-height:1.6;">%s</p>
                        </div>
                        <p style="font-size:12px;color:#777;text-align:center;margin-top:16px;">
                            Este mensaje fue generado automaticamente. No responder a este correo.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(tituloSeguro, tituloSeguro, mensajeSeguro);
    }
}
