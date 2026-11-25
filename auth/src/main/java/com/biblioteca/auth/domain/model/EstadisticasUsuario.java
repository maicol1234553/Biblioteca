package com.biblioteca.auth.domain.model;

import java.util.Map;

public record EstadisticasUsuario(
        int totalUsuarios,
        Map<String, Long> usuariosPorRol,
        double edadPromedio
) {
}
