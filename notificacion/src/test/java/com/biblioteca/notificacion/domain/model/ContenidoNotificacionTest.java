package com.biblioteca.notificacion.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContenidoNotificacionTest {

    @Test
    void renderizaVariablesYOmiteValoresNulos() {
        String plantilla = "Hola {{nombre}}, {{mensaje}} {{faltante}}";

        String resultado = PlantillaRenderer.render(
                plantilla,
                Map.of("nombre", "Ana", "mensaje", "bienvenida")
        );

        assertEquals("Hola Ana, bienvenida {{faltante}}", resultado);
        assertEquals("", PlantillaRenderer.render(null, Map.of()));
    }

    @Test
    void escapaHtml() {
        assertEquals("&lt;b&gt;Ana &amp; Luis&lt;/b&gt;", HtmlUtil.escape("<b>Ana & Luis</b>"));
        assertEquals("", HtmlUtil.escape(null));
    }

    @Test
    void generaHtmlConTituloYMensajeEscapados() {
        String html = ContenidoNotificacion.html("Titulo", "Mensaje <script>");

        assertTrue(html.contains("Titulo"));
        assertTrue(html.contains("Mensaje &lt;script&gt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    void resuelveAsuntosYMensajesPorDefecto() {
        assertEquals("Confirmacion de prestamo",
                ContenidoNotificacion.asuntoPorDefecto(TipoNotificacion.PRESTAMO_CONFIRMADO));
        assertEquals("Recordatorio de devolucion",
                ContenidoNotificacion.asuntoPorDefecto(TipoNotificacion.DEVOLUCION_PROXIMA));
        assertTrue(ContenidoNotificacion.mensajePorDefecto(TipoNotificacion.PRESTAMO_VENCIDO)
                .contains("vencido"));
    }

    @Test
    void normalizaTiposDesconocidos() {
        assertEquals(TipoNotificacion.GENERICA, TipoNotificacion.desde("otro-tipo"));
        assertEquals(TipoNotificacion.PRESTAMO_CONFIRMADO,
                TipoNotificacion.desde("prestamo confirmado"));
        assertEquals(TipoNotificacion.GENERICA, TipoNotificacion.desde(null));
    }
}
