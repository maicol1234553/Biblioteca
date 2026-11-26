package com.biblioteca.notificacion.infraestructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Api-Key";

    private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);

    private final String apiKey;

    public InternalApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("INTERNAL_API_KEY no esta configurada; las rutas internas de notificacion quedan sin proteccion");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!requiereProteccion(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String recibida = request.getHeader(HEADER);
        if (recibida == null || !coincide(recibida)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"mensaje\":\"No autorizado\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiereProteccion(HttpServletRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String ruta = request.getRequestURI();
        return ruta.startsWith("/api/notificaciones")
                || ruta.startsWith("/notificaciones")
                || ruta.startsWith("/api/biblioteca/notificaciones");
    }

    private boolean coincide(String recibida) {
        return MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                recibida.getBytes(StandardCharsets.UTF_8)
        );
    }
}
