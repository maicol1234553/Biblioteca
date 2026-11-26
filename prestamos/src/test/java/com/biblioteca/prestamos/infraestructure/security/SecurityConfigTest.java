package com.biblioteca.prestamos.infraestructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    @MockitoBean
    private UsuarioHeaderFilter usuarioHeaderFilter;

    @BeforeEach
    void configurarFiltroSimulado() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            String usuario = request.getHeader("X-Test-User");
            if (usuario != null && !usuario.isBlank()) {
                String rol = request.getHeader("X-Test-Role");
                if (rol == null) {
                    rol = "USUARIO";
                }

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                        )
                );
            }

            chain.doFilter(request, response);
            return null;
        }).when(usuarioHeaderFilter).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class),
                any(FilterChain.class)
        );
    }

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/actuator/health",
            "/actuator/info"
    })
    void rutasActuator_sinAutenticacion_debenPermitirAcceso(String ruta)
            throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/biblioteca/prestamos",
            "/api/biblioteca/prestamos/prueba",
            "/prestamos",
            "/prestamos/prueba",
            "/ruta-privada"
    })
    void rutasProtegidas_sinAutenticacion_debenResponder401(String ruta)
            throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(
                        "{\"mensaje\":\"Debe autenticarse para realizar esta operacion\"}"
                ));
    }

    @Test
    void rutaProtegida_conAutenticacion_debePermitirAcceso() throws Exception {
        mockMvc.perform(get("/api/biblioteca/prestamos")
                        .header("X-Test-User", "usuario-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("ruta autenticada"));
    }

    @Test
    void postSinTokenCsrf_conAutenticacion_debePermitirAcceso() throws Exception {
        mockMvc.perform(post("/api/biblioteca/prestamos")
                        .header("X-Test-User", "usuario-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("préstamo creado"));
    }

    @Test
    void rutaConRestriccionDeMetodo_cuandoRolEsInsuficiente_debeResponder403()
            throws Exception {
        mockMvc.perform(get("/admin/prueba")
                        .header("X-Test-User", "usuario-1")
                        .header("X-Test-Role", "USUARIO"))
                .andExpect(status().isForbidden())
                .andExpect(content().json(
                        "{\"mensaje\":\"No tiene permisos para realizar esta operacion\"}"
                ));
    }

    @Test
    void cors_debePermitirOrigenYMetodoConfigurados() throws Exception {
        String origen = "https://cliente.ejemplo.com";

        mockMvc.perform(options("/api/biblioteca/prestamos")
                        .header("Origin", origen)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "X-Test-User"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origen))
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")
                ));
    }

    @RestController
    public static class SecurityTestController {

        @GetMapping("/actuator/health")
        String health() {
            return "health";
        }

        @GetMapping("/actuator/info")
        String info() {
            return "info";
        }

        @GetMapping({
                "/api/biblioteca/prestamos",
                "/api/biblioteca/prestamos/{id}",
                "/prestamos",
                "/prestamos/{id}",
                "/ruta-privada"
        })
        String rutaAutenticada() {
            return "ruta autenticada";
        }

        @PostMapping("/api/biblioteca/prestamos")
        String crearPrestamo() {
            return "préstamo creado";
        }

        @GetMapping("/admin/prueba")
        @PreAuthorize("hasRole('ADMINISTRADOR')")
        String rutaAdministrador() {
            return "admin";
        }
    }
}