package com.biblioteca.notificacion.infraestructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InternalApiKeyFilterTest {

    @Test
    void sinClaveConfigurada_permiteLaRutaInterna() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/notificaciones/enviar-correo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void conClaveConfigurada_sinHeader_devuelve401() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("clave-interna");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/notificaciones/enviar-correo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void conClaveConfigurada_headerIncorrecto_devuelve401() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("clave-interna");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/notificaciones/enviar-correo");
        request.addHeader(InternalApiKeyFilter.HEADER, "otra-clave");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void conClaveConfigurada_headerCorrecto_permiteLaRuta() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("clave-interna");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/biblioteca/notificaciones/evento");
        request.addHeader(InternalApiKeyFilter.HEADER, "clave-interna");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void conClaveConfigurada_preflight_permiteLaRuta() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("clave-interna");
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/notificaciones/enviar-correo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void conClaveConfigurada_rutaPublica_permiteLaRuta() throws Exception {
        InternalApiKeyFilter filter = new InternalApiKeyFilter("clave-interna");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }
}
