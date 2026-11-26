package com.biblioteca.catalogo.infraestructure.security;

import com.biblioteca.catalogo.domain.model.Rol;
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
public class RolHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private final UsuarioRolJdbcAdapter usuarioRolJdbcAdapter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idUsuario = request.getHeader(HEADER_USUARIO);

        if (idUsuario != null && !idUsuario.isBlank()) {
            String rol = usuarioRolJdbcAdapter.buscarRol(idUsuario.trim());

            if (rol == null) {
                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuario no autorizado");
                return;
            }

            Rol rolValido;

            try {
                rolValido = Rol.desde(rol);
            } catch (IllegalArgumentException exception) {
                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "El usuario no tiene un rol valido");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            idUsuario.trim(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rolValido.name())));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void escribirError(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
