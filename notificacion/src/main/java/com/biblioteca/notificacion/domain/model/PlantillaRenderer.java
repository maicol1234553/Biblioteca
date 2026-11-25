package com.biblioteca.notificacion.domain.model;

import java.util.Map;

public final class PlantillaRenderer {

    private PlantillaRenderer() {
    }

    /**
     * Reemplaza variables con el formato {{clave}}. Los valores deben llegar
     * ya escapados cuando se insertan en contenido HTML.
     */
    public static String render(String plantilla, Map<String, String> variables) {
        if (plantilla == null) {
            return "";
        }

        String resultado = plantilla;

        for (Map.Entry<String, String> variable : variables.entrySet()) {
            resultado = resultado.replace(
                    "{{" + variable.getKey() + "}}",
                    variable.getValue() == null ? "" : variable.getValue()
            );
        }

        return resultado;
    }
}
