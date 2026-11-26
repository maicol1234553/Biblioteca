package com.biblioteca.notificacion.domain.model;

public record PoliticaNotificaciones(
        int maxReintentos,
        int diasAnticipacionRecordatorio,
        /** Codigo de pais sin '+' que se antepone a los 10 digitos de Auth. */
        String codigoPais,
        /** Canal usado cuando el solicitante no pide uno explicito. */
        CanalNotificacion canalPorDefecto
) {
}
