package com.biblioteca.prestamos.infraestructure.security;

import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.model.Rol;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;





@Component
@RequiredArgsConstructor
public class UsuarioHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private final AuthGateway authGateway;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idUsuario = request.getHeader(HEADER_USUARIO);

        if (idUsuario == null || idUsuario.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UsuarioExterno usuario = authGateway.buscarUsuario(idUsuario.trim());

            if (usuario == null) {
                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuario no autorizado");
                return;
            }

            Rol rol;

            try {
                rol = Rol.desde(usuario.rol());
            } catch (IllegalArgumentException exception) {
                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "El usuario no tiene un rol valido");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ServicioExternoNoDisponibleException exception) {
            escribirError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudo validar el usuario con el microservicio Auth");
        }
    }

    private void escribirError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
