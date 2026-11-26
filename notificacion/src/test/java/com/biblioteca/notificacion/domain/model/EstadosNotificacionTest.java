package com.biblioteca.notificacion.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadosNotificacionTest {

    @Test
    void estadoNotificacionNormalizaYRechazaInvalidos() {
        assertEquals(EstadoNotificacion.PENDIENTE, EstadoNotificacion.desde(" pendiente "));
        assertEquals(EstadoNotificacion.ENVIADA, EstadoNotificacion.desde("enviada"));
        assertThrows(IllegalArgumentException.class, () -> EstadoNotificacion.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoNotificacion.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoNotificacion.desde("OTRO"));
    }

    @Test
    void estadoRecordatorioNormalizaYRechazaInvalidos() {
        assertEquals(EstadoRecordatorio.PENDIENTE, EstadoRecordatorio.desde(" pendiente "));
        assertEquals(EstadoRecordatorio.PROCESADO, EstadoRecordatorio.desde("procesado"));
        assertThrows(IllegalArgumentException.class, () -> EstadoRecordatorio.desde(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoRecordatorio.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoRecordatorio.desde("OTRO"));
    }

    @Test
    void resultadoEnvioNormalizaYRechazaInvalidos() {
        assertEquals(ResultadoEnvio.EXITOSO, ResultadoEnvio.desde(" exitoso "));
        assertEquals(ResultadoEnvio.FALLIDO, ResultadoEnvio.desde("fallido"));
        assertThrows(IllegalArgumentException.class, () -> ResultadoEnvio.desde(null));
        assertThrows(IllegalArgumentException.class, () -> ResultadoEnvio.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> ResultadoEnvio.desde("OTRO"));
    }

    @Test
    void estadoPlantillaUsaActivaPorDefectoYRechazaInvalidos() {
        assertEquals(EstadoPlantilla.ACTIVA, EstadoPlantilla.desde(null));
        assertEquals(EstadoPlantilla.ACTIVA, EstadoPlantilla.desde(" "));
        assertEquals(EstadoPlantilla.INACTIVA, EstadoPlantilla.desde(" inactiva "));
        assertThrows(IllegalArgumentException.class, () -> EstadoPlantilla.desde("OTRO"));
    }
}
