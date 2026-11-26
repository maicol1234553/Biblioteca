package com.biblioteca.prestamos.infraestructure.security;

import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.model.Rol;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.model.gateway.AuthGateway;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsuarioHeaderFilterTest {

    private final AuthGateway authGateway = mock(AuthGateway.class);
    private final UsuarioHeaderFilter filter = new UsuarioHeaderFilter(authGateway);

    @BeforeEach
    void limpiarContextoAntesDeLaPrueba() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContextoDespuesDeLaPrueba() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_cuandoFaltaElHeader_debeContinuarSinConsultarAuth()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        verifyNoInteractions(authGateway);
        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_cuandoHeaderEstaEnBlanco_debeContinuarSinConsultarAuth()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, "   ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        verifyNoInteractions(authGateway);
        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR, ROLE_ADMINISTRADOR",
            "USUARIO, ROLE_USUARIO",
            "ADMIN, ROLE_ADMINISTRADOR",
            "USER, ROLE_USUARIO"
    })
    void doFilter_cuandoUsuarioTieneRolValido_debeAutenticarYContinuar(
            String rol,
            String autoridadEsperada
    ) throws Exception {

        UsuarioExterno usuario = mock(UsuarioExterno.class);
        when(usuario.rol()).thenReturn(rol);
        when(authGateway.buscarUsuario("usuario-1")).thenReturn(usuario);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, " usuario-1 ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        assertSame(usuario, autenticacion.getPrincipal());
        assertTrue(autenticacion.isAuthenticated());
        assertTrue(autenticacion.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals(autoridadEsperada)
        ));

        verify(authGateway).buscarUsuario("usuario-1");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_cuandoAuthNoEncuentraUsuario_debeResponder401()
            throws Exception {

        when(authGateway.buscarUsuario("usuario-1")).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        assertEquals(401, response.getStatus());
        assertEquals(
                "{\"mensaje\":\"Usuario no autorizado\"}",
                response.getContentAsString()
        );

        verifyNoInteractions(chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_cuandoRolNoEsValido_debeResponder401() throws Exception {

        UsuarioExterno usuario = mock(UsuarioExterno.class);
        when(usuario.rol()).thenReturn("INVITADO");
        when(authGateway.buscarUsuario("usuario-1")).thenReturn(usuario);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        assertEquals(401, response.getStatus());
        assertEquals(
                "{\"mensaje\":\"El usuario no tiene un rol valido\"}",
                response.getContentAsString()
        );

        verifyNoInteractions(chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_cuandoRolEsNull_debeAsignarRolUsuario() throws Exception {

        UsuarioExterno usuario = mock(UsuarioExterno.class);
        when(usuario.rol()).thenReturn(null);
        when(authGateway.buscarUsuario("usuario-1")).thenReturn(usuario);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        assertTrue(autenticacion.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_USUARIO")
        ));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_cuandoAuthNoEstaDisponible_debeResponder503() throws Exception {

        when(authGateway.buscarUsuario("usuario-1"))
                .thenThrow(new ServicioExternoNoDisponibleException(
                        "Auth no disponible"
                ));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(UsuarioHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        assertEquals(503, response.getStatus());
        assertEquals(
                "{\"mensaje\":\"No se pudo validar el usuario con el microservicio Auth\"}",
                response.getContentAsString()
        );

        verifyNoInteractions(chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}