package com.biblioteca.notificacion.domain.model;

/**
 * Normalizacion de numeros telefonicos al formato E.164 que exige el proveedor
 * de SMS. Auth almacena los numeros colombianos con 10 digitos sin prefijo
 * (por ejemplo 3001234567), por lo que se les antepone el codigo de pais.
 */
public final class Telefono {

    private static final String EXPRESION_DIGITOS = "\\d{7,15}";

    private Telefono() {
    }

    /**
     * Devuelve el numero en formato +codigopaisnumero o null cuando el valor no
     * es un telefono utilizable (por ejemplo un correo electronico).
     */
    public static String normalizar(String valor, String codigoPais) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String limpio = valor.trim().replaceAll("[\\s().-]", "");

        if (limpio.startsWith("+")) {
            limpio = limpio.substring(1);
        } else if (limpio.startsWith("00")) {
            limpio = limpio.substring(2);
        }

        if (!limpio.matches(EXPRESION_DIGITOS)) {
            return null;
        }

        String prefijo = codigoPais == null ? "" : codigoPais.trim().replace("+", "");

        if (!prefijo.isEmpty() && limpio.length() == 10) {
            limpio = prefijo + limpio;
        }

        if (!limpio.matches(EXPRESION_DIGITOS)) {
            return null;
        }

        return "+" + limpio;
    }
}
