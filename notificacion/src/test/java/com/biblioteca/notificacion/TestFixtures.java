package com.biblioteca.notificacion;

import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.model.PoliticaNotificaciones;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.model.UsuarioContacto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class TestFixtures {

    public static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 1, 15, 10, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    public static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);
    public static final PoliticaNotificaciones POLITICA = new PoliticaNotificaciones(3, 1);

    private TestFixtures() {
    }

    public static UsuarioContacto contacto(String id) {
        return new UsuarioContacto(id, "Usuario " + id, id + "@example.com");
    }

    public static Notificacion notificacion(String id,
                                            String idUsuario,
                                            String tipo,
                                            String estado,
                                            String destinatario) {
        return new Notificacion(
                id,
                idUsuario,
                destinatario,
                "Asunto de prueba",
                "<html><body><p>Mensaje de prueba</p></body></html>",
                tipo,
                AHORA.minusHours(1),
                null,
                estado
        );
    }

    public static PlantillaNotificacion plantilla(String id, String tipo) {
        return new PlantillaNotificacion(
                id,
                "Plantilla " + tipo,
                "Asunto {{nombre}}",
                "<html><body><p>{{mensaje}}</p></body></html>",
                tipo,
                "ACTIVA"
        );
    }

    public static Recordatorio recordatorio(String id,
                                            String idUsuario,
                                            String idPrestamo,
                                            LocalDateTime fecha,
                                            String tipo,
                                            String estado) {
        return new Recordatorio(id, idUsuario, idPrestamo, fecha, tipo, estado);
    }

    public static HistorialEnvio historial(String id,
                                           String idNotificacion,
                                           String resultado) {
        return new HistorialEnvio(
                id,
                idNotificacion,
                AHORA.minusMinutes(5),
                resultado,
                "FALLIDO".equals(resultado) ? "Error SMTP" : null
        );
    }
}
