package com.biblioteca.auth.infraestructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.SecurityTestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rutaDeUsuario_sinAutenticacion_debePermitirAcceso() throws Exception {
        // Arrange
        String ruta = "/api/biblioteca/usuario/prueba";

        // Act
        var resultado = mockMvc.perform(get(ruta));

        // Assert
        resultado
                .andExpect(status().isOk())
                .andExpect(content().string("ruta publica"));
    }

    @Test
    void rutaDeUsuarioPost_sinTokenCsrf_debePermitirAcceso() throws Exception {
        // Arrange
        String ruta = "/api/biblioteca/usuario/prueba";

        // Act
        var resultado = mockMvc.perform(post(ruta)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // Assert
        resultado
                .andExpect(status().isOk())
                .andExpect(content().string("post publico"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/actuator/health",
            "/actuator/health/",
            "/actuator/health/liveness",
            "/actuator/health/liveness/readiness"
    })
    void rutaDeHealth_sinAutenticacion_debePermitirAcceso(String ruta) throws Exception {
        // Arrange
        // La ruta se recibe como parámetro.

        // Act
        var resultado = mockMvc.perform(get(ruta));

        // Assert
        resultado
                .andExpect(status().isOk())
                .andExpect(content().string("health publico"));
    }

    @Test
    void rutaPrivada_sinAutenticacion_debeDenegarAcceso() throws Exception {
        // Arrange
        String ruta = "/api/biblioteca/libros/prueba";

        // Act
        var resultado = mockMvc.perform(get(ruta));

        // Assert
        resultado.andExpect(status().isForbidden());
    }

    @RestController
    static class SecurityTestController {

        @GetMapping("/api/biblioteca/usuario/prueba")
        String rutaUsuarioGet() {
            return "ruta publica";
        }

        @PostMapping("/api/biblioteca/usuario/prueba")
        String rutaUsuarioPost() {
            return "post publico";
        }

        @GetMapping("/api/biblioteca/libros/prueba")
        String rutaPrivada() {
            return "ruta privada";
        }

        @GetMapping({
                "/actuator/health",
                "/actuator/health/",
                "/actuator/health/{*path}"
        })
        String rutaHealth() {
            return "health publico";
        }
    }
}