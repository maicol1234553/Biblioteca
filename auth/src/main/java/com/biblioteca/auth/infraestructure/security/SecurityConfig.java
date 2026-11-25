package com.biblioteca.auth.infraestructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Rutas publicas del microservicio Auth.
     *
     * - /api/biblioteca/usuario/**  -> todos los endpoints de UsuarioController
     *   (save, login, buscar, update, delete, cambiar-password, recuperar-password,
     *   restablecer-password, rol, estadisticas).
     * - /actuator/health, /actuator/health/ y /actuator/health/** -> health y
     *   subgrupos (liveness/readiness) usados por Render.
     */
    private static final String[] RUTAS_PUBLICAS = {
            "/api/biblioteca/usuario/**",
            "/actuator/health",
            "/actuator/health/",
            "/actuator/health/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .build();
    }
}
