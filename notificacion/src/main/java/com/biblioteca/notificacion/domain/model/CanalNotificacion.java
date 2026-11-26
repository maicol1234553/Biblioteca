package com.biblioteca.notificacion.domain.model;

/**
 * Canal por el que se entrega una notificacion. SMS es el canal por defecto;
 * CORREO se usa en el endpoint explicito /enviar-correo y como respaldo cuando
 * el usuario no tiene telefono registrado.
 */
public enum CanalNotificacion {
    CORREO,
    SMS;

    /**
     * Convierte el valor recibido en la peticion. Lanza IllegalArgumentException
     * cuando el canal no existe, de forma que se responda HTTP 400.
     */
    public static CanalNotificacion desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Canal de notificacion invalido: " + valor);
        }
    }

    /**
     * Lee el valor persistido. Las filas anteriores a la migracion tienen el
     * canal nulo y se tratan como CORREO para no perder los reintentos.
     */
    public static CanalNotificacion desdeGuardado(String valor) {
        CanalNotificacion canal = desde(valor);
        return canal == null ? CORREO : canal;
    }
}
