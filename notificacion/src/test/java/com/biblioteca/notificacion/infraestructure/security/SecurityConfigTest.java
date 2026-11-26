package com.biblioteca.notificacion.infraestructure.security;

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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.SecurityTestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {
            "/actuator/prueba",
            "/api/notificaciones/prueba",
            "/api/biblioteca/notificaciones/prueba",
            "/notificaciones/prueba"
    })
    void rutasConfiguradas_sinAutenticacion_debenPermitirAcceso(String ruta)
            throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(content().string("ruta publica"));
    }

    @Test
    void cualquierOtraRuta_sinTokenCsrf_debePermitirAcceso() throws Exception {
        mockMvc.perform(post("/ruta-privada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("post permitido"));
    }

    @Test
    void cors_debePermitirOrigenYMetodoConfigurados() throws Exception {
        String origen = "https://cliente.ejemplo.com";

        mockMvc.perform(options("/api/notificaciones/prueba")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Prueba"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origen))
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        containsString("GET")
                ));
    }

    @RestController
    public static class SecurityTestController {

        @GetMapping({
                "/actuator/prueba",
                "/api/notificaciones/prueba",
                "/api/biblioteca/notificaciones/prueba",
                "/notificaciones/prueba"
        })
        String rutaPublica() {
            return "ruta publica";
        }

        @PostMapping("/ruta-privada")
        String rutaPost() {
            return "post permitido";
        }
    }
}