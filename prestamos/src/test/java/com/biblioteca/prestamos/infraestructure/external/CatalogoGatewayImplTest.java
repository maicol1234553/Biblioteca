package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.prestamos.domain.model.DisponibilidadLibroExterna;
import com.biblioteca.prestamos.domain.model.EjemplarExterno;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CatalogoGatewayImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PrestamosProperties properties;

    private MockRestServiceServer servidor;
    private CatalogoGatewayImpl catalogoGateway;

    @BeforeEach
    void setUp() {
        // Arrange común
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://catalogo.test");

        servidor = MockRestServiceServer.bindTo(builder).build();

        catalogoGateway = new CatalogoGatewayImpl(
                builder.build(),
                new ObjectMapper(),
                properties
        );
    }

    @AfterEach
    void verificarSolicitudes() {
        servidor.verify();
    }

    @Test
    void buscarEjemplar_cuandoCatalogoDevuelveEjemplar_debeMapearSusDatos() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "idEjemplar": "ejemplar-1",
                          "idLibro": "libro-1",
                          "tituloLibro": "Cien años de soledad",
                          "codigoInventario": "INV-001",
                          "ubicacion": "Estante A",
                          "estado": "DISPONIBLE"
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        EjemplarExterno resultado =
                catalogoGateway.buscarEjemplar("ejemplar-1");

        // Assert
        assertEquals("ejemplar-1", resultado.idEjemplar());
        assertEquals("libro-1", resultado.idLibro());
        assertEquals("Cien años de soledad", resultado.tituloLibro());
        assertEquals("INV-001", resultado.codigoInventario());
        assertEquals("Estante A", resultado.ubicacion());
        assertEquals("DISPONIBLE", resultado.estado());
    }

    @Test
    void buscarEjemplar_cuandoCatalogoRetorna404_debeRetornarNull() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-inexistente",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act
        EjemplarExterno resultado =
                catalogoGateway.buscarEjemplar("ejemplar-inexistente");

        // Assert
        assertNull(resultado);
    }

    @Test
    void buscarEjemplar_cuandoElJsonNoTieneId_debeRetornarNull() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "idLibro": "libro-1",
                          "estado": "DISPONIBLE"
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        EjemplarExterno resultado =
                catalogoGateway.buscarEjemplar("ejemplar-1");

        // Assert
        assertNull(resultado);
    }

    @Test
    void buscarEjemplar_cuandoCatalogoRetornaErrorDeServidor_debeLanzarExcepcion() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> catalogoGateway.buscarEjemplar("ejemplar-1")
        );

        // Assert
        assertEquals(
                "Catalogo respondio con error al consultar el ejemplar",
                excepcion.getMessage()
        );
    }

    @Test
    void buscarEjemplar_cuandoFallaLaConexion_debeLanzarExcepcion() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException("Conexión rechazada")));

        // Act
        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> catalogoGateway.buscarEjemplar("ejemplar-1")
        );

        // Assert
        assertEquals(
                "No se pudo consultar el microservicio Catalogo",
                excepcion.getMessage()
        );
    }

    @Test
    void consultarDisponibilidadLibro_cuandoCatalogoDevuelveDatos_debeMapearlos() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/libros/libro-1/disponibilidad",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "idLibro": "libro-1",
                          "titulo": "Cien años de soledad",
                          "totalEjemplares": 5,
                          "disponibles": 2,
                          "disponible": true
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        DisponibilidadLibroExterna resultado =
                catalogoGateway.consultarDisponibilidadLibro("libro-1");

        // Assert
        assertEquals("libro-1", resultado.idLibro());
        assertEquals("Cien años de soledad", resultado.titulo());
        assertEquals(5, resultado.totalEjemplares());
        assertEquals(2, resultado.disponibles());
        assertEquals(true, resultado.disponible());
    }

    @Test
    void consultarDisponibilidadLibro_cuandoCatalogoRetorna404_debeRetornarNull() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/libros/libro-inexistente/disponibilidad",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act
        DisponibilidadLibroExterna resultado =
                catalogoGateway.consultarDisponibilidadLibro("libro-inexistente");

        // Assert
        assertNull(resultado);
    }

    @Test
    void consultarDisponibilidadLibro_cuandoFaltanCantidades_debeUsarCeroComoValorPredeterminado() {
        // Arrange
        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/libros/libro-1/disponibilidad",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "idLibro": "libro-1",
                          "disponible": false
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        DisponibilidadLibroExterna resultado =
                catalogoGateway.consultarDisponibilidadLibro("libro-1");

        // Assert
        assertEquals(0, resultado.totalEjemplares());
        assertEquals(0, resultado.disponibles());
        assertEquals(false, resultado.disponible());
    }

    @Test
    void actualizarEstadoEjemplar_cuandoAdminEstaConfigurado_debeEnviarSolicitudPut() {
        // Arrange
        when(properties.servicios().catalogoAdminUsuarioId())
                .thenReturn("  admin-1  ");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("X-Usuario-Id", "admin-1"))
                .andExpect(content().json("""
                        {
                          "estado": "PRESTADO"
                        }
                        """))
                .andRespond(withSuccess());

        // Act
        catalogoGateway.actualizarEstadoEjemplar("ejemplar-1", "PRESTADO");

        // Assert
        // servidor.verify() en @AfterEach confirma que se realizó la solicitud esperada.
    }

    @Test
    void actualizarEstadoEjemplar_cuandoAdminNoEstaConfigurado_debeLanzarExcepcion() {
        // Arrange
        when(properties.servicios().catalogoAdminUsuarioId())
                .thenReturn("   ");

        // Act
        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> catalogoGateway.actualizarEstadoEjemplar(
                        "ejemplar-1",
                        "PRESTADO"
                )
        );

        // Assert
        assertEquals(
                "Configure prestamos.servicios.catalogo-admin-usuario-id con un usuario "
                        + "ADMINISTRADOR para actualizar el estado de los ejemplares",
                excepcion.getMessage()
        );
    }

    @Test
    void actualizarEstadoEjemplar_cuandoCatalogoFalla_debeLanzarExcepcion() {
        // Arrange
        when(properties.servicios().catalogoAdminUsuarioId())
                .thenReturn("admin-1");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/ejemplares/ejemplar-1",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        ServicioExternoNoDisponibleException excepcion = assertThrows(
                ServicioExternoNoDisponibleException.class,
                () -> catalogoGateway.actualizarEstadoEjemplar(
                        "ejemplar-1",
                        "PRESTADO"
                )
        );

        // Assert
        assertEquals(
                "No se pudo actualizar el estado del ejemplar en Catalogo",
                excepcion.getMessage()
        );
    }
}