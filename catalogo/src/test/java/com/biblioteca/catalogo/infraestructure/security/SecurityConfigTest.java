package com.biblioteca.catalogo.infraestructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        RolHeaderFilter.class,
        SecurityConfigTest.SecurityTestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRolJdbcAdapter usuarioRolJdbcAdapter;

    @Test
    void getDelCatalogo_sinAutenticacion_debePermitirAcceso() throws Exception {
        mockMvc.perform(get("/api/biblioteca/libros"))
                .andExpect(status().isOk())
                .andExpect(content().string("catalogo publico"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/actuator/info"})
    void rutasActuator_sinAutenticacion_debenPermitirAcceso(String ruta)
            throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isOk());
    }

    @Test
    void postDelCatalogo_sinAutenticacion_debeResponder401() throws Exception {
        mockMvc.perform(post("/api/biblioteca/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(
                        "{\"mensaje\":\"Debe autenticarse para realizar esta operacion\"}"
                ));
    }

    @Test
    void postDelCatalogo_conRolUsuario_debeResponder403() throws Exception {
        when(usuarioRolJdbcAdapter.buscarRol("usuario-1"))
                .thenReturn("USUARIO");

        mockMvc.perform(post("/api/biblioteca/libros")
                        .header(RolHeaderFilter.HEADER_USUARIO, "usuario-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(content().json(
                        "{\"mensaje\":\"No tiene permisos de administrador\"}"
                ));
    }

    @Test
    void postDelCatalogo_conRolAdministrador_debePermitirAccesoSinCsrf()
            throws Exception {
        when(usuarioRolJdbcAdapter.buscarRol("admin-1"))
                .thenReturn("ADMINISTRADOR");

        mockMvc.perform(post("/api/biblioteca/libros")
                        .header(RolHeaderFilter.HEADER_USUARIO, "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("libro creado"));
    }

    @Test
    void rutaFueraDelCatalogo_sinAutenticacion_debeResponder401() throws Exception {
        mockMvc.perform(get("/ruta-privada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaFueraDelCatalogo_conUsuarioAutenticado_debePermitirAcceso()
            throws Exception {
        when(usuarioRolJdbcAdapter.buscarRol("usuario-1"))
                .thenReturn("USUARIO");

        mockMvc.perform(get("/ruta-privada")
                        .header(RolHeaderFilter.HEADER_USUARIO, "usuario-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("ruta autenticada"));
    }

    @Test
    void rutaPublica_conRolInvalido_debeResponder401() throws Exception {
        when(usuarioRolJdbcAdapter.buscarRol("usuario-1"))
                .thenReturn("INVITADO");

        mockMvc.perform(get("/api/biblioteca/libros")
                        .header(RolHeaderFilter.HEADER_USUARIO, "usuario-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(
                        "{\"mensaje\":\"El usuario no tiene un rol valido\"}"
                ));
    }

    @Test
    void cors_debePermitirOrigenYMetodoPost() throws Exception {
        String origen = "https://cliente.ejemplo.com";

        mockMvc.perform(options("/api/biblioteca/libros")
                        .header(HttpHeaders.ORIGIN, origen)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "X-Usuario-Id, Content-Type"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        origen
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        org.hamcrest.Matchers.containsString("POST")
                ));
    }

    @RestController
    public static class SecurityTestController {

        @GetMapping("/api/biblioteca/libros")
        String listarLibros() {
            return "catalogo publico";
        }

        @PostMapping("/api/biblioteca/libros")
        String crearLibro() {
            return "libro creado";
        }

        @GetMapping("/actuator/health")
        String health() {
            return "health";
        }

        @GetMapping("/actuator/info")
        String info() {
            return "info";
        }

        @GetMapping("/ruta-privada")
        String rutaPrivada() {
            return "ruta autenticada";
        }
    }
}