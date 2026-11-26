package com.biblioteca.prestamos.infraestructure.external;

import com.biblioteca.prestamos.domain.model.EventoNotificacion;
import com.biblioteca.prestamos.infraestructure.config.PrestamosProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class NotificacionGatewayImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PrestamosProperties properties;

    private MockRestServiceServer servidor;
    private NotificacionGatewayImpl notificacionGateway;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://notificacion.test");

        servidor = MockRestServiceServer.bindTo(builder).build();

        notificacionGateway = new NotificacionGatewayImpl(
                builder.build(),
                properties
        );
    }

    @AfterEach
    void verificarSolicitudes() {
        servidor.verify();
    }

    @Test
    void enviarEvento_cuandoNotificacionesEstanHabilitadas_debeEnviarEventoComoJson() throws Exception {

        EventoNotificacion evento = crearEvento();
        String tipo = ((Enum<?>) evento.tipo()).name();

        when(properties.servicios().notificacionHabilitada()).thenReturn(true);
        when(properties.servicios().notificacionPath())
                .thenReturn("/api/biblioteca/notificaciones/evento");

        servidor.expect(request ->
                        assertEquals(
                                "/api/biblioteca/notificaciones/evento",
                                request.getURI().getPath()
                        ))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "tipo": "%s",
                          "idUsuario": "usuario-1",
                          "referenciaId": "prestamo-1",
                          "mensaje": "Préstamo confirmado",
                          "fecha": null
                        }
                        """.formatted(tipo)))
                .andRespond(withSuccess());


        notificacionGateway.enviarEvento(evento);



    }

    @Test
    void enviarEvento_cuandoNotificacionesEstanDeshabilitadas_noDebeEnviarSolicitud() {

        when(properties.servicios().notificacionHabilitada()).thenReturn(false);


        notificacionGateway.enviarEvento(null);



    }

    @Test
    void enviarEvento_cuandoElServicioFalla_noDebePropagarLaExcepcion() throws Exception {

        EventoNotificacion evento = crearEvento();

        when(properties.servicios().notificacionHabilitada()).thenReturn(true);
        when(properties.servicios().notificacionPath())
                .thenReturn("/api/biblioteca/notificaciones/evento");

        for (int intento = 0; intento < 3; intento++) {
            servidor.expect(request ->
                            assertEquals(
                                    "/api/biblioteca/notificaciones/evento",
                                    request.getURI().getPath()
                            ))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withServerError());
        }


        assertDoesNotThrow(() -> notificacionGateway.enviarEvento(evento));
    }

    private EventoNotificacion crearEvento() throws Exception {
        EventoNotificacion evento = org.mockito.Mockito.mock(EventoNotificacion.class);



        Class<?> tipoClase = EventoNotificacion.class
                .getMethod("tipo")
                .getReturnType();

        Object tipo = tipoClase.getEnumConstants()[0];
        doReturn(tipo).when(evento).tipo();

        when(evento.idUsuario()).thenReturn("usuario-1");
        when(evento.referenciaId()).thenReturn("prestamo-1");
        when(evento.mensaje()).thenReturn("Préstamo confirmado");

        return evento;
    }
}