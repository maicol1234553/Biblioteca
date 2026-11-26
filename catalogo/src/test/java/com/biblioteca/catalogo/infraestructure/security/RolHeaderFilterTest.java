package com.biblioteca.catalogo.infraestructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RolHeaderFilterTest {

    private final UsuarioRolJdbcAdapter usuarioRolJdbcAdapter =
            mock(UsuarioRolJdbcAdapter.class);

    private final RolHeaderFilter filter =
            new RolHeaderFilter(usuarioRolJdbcAdapter);

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_cuandoNoHayEncabezado_debeContinuarSinConsultarRol() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        verify(chain).doFilter(request, response);
        verifyNoInteractions(usuarioRolJdbcAdapter);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_cuandoEncabezadoEstaEnBlanco_debeContinuarSinConsultarRol()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RolHeaderFilter.HEADER_USUARIO, "   ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);


        filter.doFilter(request, response, chain);


        verify(chain).doFilter(request, response);
        verifyNoInteractions(usuarioRolJdbcAdapter);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @CsvSource({
            "USUARIO, ROLE_USUARIO",
            "ADMINISTRADOR, ROLE_ADMINISTRADOR",
            "USER, ROLE_USUARIO",
            "ADMIN, ROLE_ADMINISTRADOR"
    })
    void doFilter_cuandoRolEsValido_debeAutenticarYContinuar(
            String rolEncontrado,
            String autoridadEsperada
    ) throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RolHeaderFilter.HEADER_USUARIO, "  usuario-1  ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(usuarioRolJdbcAdapter.buscarRol("usuario-1"))
                .thenReturn(rolEncontrado);


        filter.doFilter(request, response, chain);


        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        assertEquals("usuario-1", autenticacion.getPrincipal());
        assertTrue(autenticacion.getAuthorities().contains(
                new SimpleGrantedAuthority(autoridadEsperada)
        ));

        verify(usuarioRolJdbcAdapter).buscarRol("usuario-1");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_cuandoUsuarioNoTieneRol_debeResponder401YSinContinuar()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RolHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(usuarioRolJdbcAdapter.buscarRol("usuario-1")).thenReturn(null);


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
    void doFilter_cuandoRolNoEsValido_debeResponder401YSinContinuar()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RolHeaderFilter.HEADER_USUARIO, "usuario-1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(usuarioRolJdbcAdapter.buscarRol("usuario-1"))
                .thenReturn("INVITADO");


        filter.doFilter(request, response, chain);


        assertEquals(401, response.getStatus());
        assertEquals(
                "{\"mensaje\":\"El usuario no tiene un rol valido\"}",
                response.getContentAsString()
        );

        verifyNoInteractions(chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}