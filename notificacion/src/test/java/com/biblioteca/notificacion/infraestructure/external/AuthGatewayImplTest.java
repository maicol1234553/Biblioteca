package com.biblioteca.notificacion.infraestructure.external;

import com.biblioteca.notificacion.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.notificacion.domain.model.UsuarioContacto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AuthGatewayImplTest {

    private MockRestServiceServer servidor;
    private AuthGatewayImpl authGateway;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://auth.test");

        servidor = MockRestServiceServer.bindTo(builder).build();

        authGateway = new AuthGatewayImpl(
                builder.build(),
                new ObjectMapper()
        );
    }

    @AfterEach
    void verificarSolicitudes() {
        servidor.verify();
    }

    @Test
    void buscarUsuario_cuandoAuthDevuelveUsuario_debeMapearSusDatos() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "idUsuario": "usuario-1",
                          "nombre": "Ana Pérez",
                          "correo": "ana@biblioteca.com"
                        }
                        """, MediaType.APPLICATION_JSON));


        UsuarioContacto resultado = authGateway.buscarUsuario("usuario-1");


        assertEquals("usuario-1", resultado.idUsuario());
        assertEquals("Ana Pérez", resultado.nombre());
        assertEquals("ana@biblioteca.com", resultado.correo());
    }

    @Test
    void buscarUsuario_cuandoAuthRetorna404_debeRetornarNull() {

        esperarConsulta("usuario-inexistente");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-inexistente",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));


        UsuarioContacto resultado =
                authGateway.buscarUsuario("usuario-inexistente");


        assertNull(resultado);
    }

    @Test
    void buscarUsuario_cuandoElCuerpoEstaVacio_debeRetornarNull() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.TEXT_PLAIN));


        UsuarioContacto resultado = authGateway.buscarUsuario("usuario-1");


        assertNull(resultado);
    }

    @Test
    void buscarUsuario_cuandoElCuerpoNoEsJson_debeRetornarNull() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("Respuesta no JSON", MediaType.TEXT_PLAIN));


        UsuarioContacto resultado = authGateway.buscarUsuario("usuario-1");


        assertNull(resultado);
    }

    @Test
    void buscarUsuario_cuandoElJsonNoTieneId_debeRetornarNull() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "nombre": "Ana Pérez",
                          "correo": "ana@biblioteca.com"
                        }
                        """, MediaType.APPLICATION_JSON));


        UsuarioContacto resultado = authGateway.buscarUsuario("usuario-1");


        assertNull(resultado);
    }

    @Test
    void buscarUsuario_cuandoAuthRetornaErrorDistintoDe404_debeLanzarExcepcion() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));


        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> authGateway.buscarUsuario("usuario-1")
        );


        assertEquals(
                "Auth respondio con error al consultar el usuario",
                excepcion.getMessage()
        );
    }

    @Test
    void buscarUsuario_cuandoJsonEsInvalido_debeLanzarExcepcion() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{json-invalido", MediaType.APPLICATION_JSON));


        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> authGateway.buscarUsuario("usuario-1")
        );


        assertEquals(
                "Auth devolvio una respuesta invalida",
                excepcion.getMessage()
        );
    }

    @Test
    void buscarUsuario_cuandoFallaLaConexion_debeLanzarExcepcion() {

        esperarConsulta("usuario-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/usuario/id/usuario-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Conexión rechazada")));


        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> authGateway.buscarUsuario("usuario-1")
        );


        assertEquals(
                "No se pudo consultar el microservicio Auth",
                excepcion.getMessage()
        );
    }

    private void esperarConsulta(String idUsuario) {


    }
}