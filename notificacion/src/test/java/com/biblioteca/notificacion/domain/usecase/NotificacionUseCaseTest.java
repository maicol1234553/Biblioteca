package com.biblioteca.notificacion.domain.usecase;

import com.biblioteca.notificacion.TestFixtures;
import com.biblioteca.notificacion.domain.exception.DestinatarioInvalidoException;
import com.biblioteca.notificacion.domain.exception.EnvioCorreoException;
import com.biblioteca.notificacion.domain.exception.EstadoInvalidoException;
import com.biblioteca.notificacion.domain.exception.LimiteReintentosExcedidoException;
import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.exception.ServicioExternoNoDisponibleException;
import com.biblioteca.notificacion.domain.model.EventoNotificacion;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.gateway.AuthGateway;
import com.biblioteca.notificacion.domain.model.gateway.CorreoGateway;
import com.biblioteca.notificacion.domain.model.gateway.HistorialEnvioGateway;
import com.biblioteca.notificacion.domain.model.gateway.NotificacionGateway;
import com.biblioteca.notificacion.domain.model.gateway.PlantillaNotificacionGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionUseCaseTest {

    @Mock
    private NotificacionGateway notificacionGateway;
    @Mock
    private HistorialEnvioGateway historialEnvioGateway;
    @Mock
    private PlantillaNotificacionGateway plantillaGateway;
    @Mock
    private AuthGateway authGateway;
    @Mock
    private CorreoGateway correoGateway;

    private NotificacionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new NotificacionUseCase(
                notificacionGateway,
                historialEnvioGateway,
                plantillaGateway,
                authGateway,
                correoGateway,
                TestFixtures.POLITICA,
                TestFixtures.CLOCK
        );
    }

    @Test
    void enviarCorreo_exitosoConDestinatario() {
        prepararGuardado();

        Notificacion notificacion = useCase.enviarCorreo(
                "u1", "user@example.com", "Asunto", "Mensaje", "GENERICA");

        assertEquals("ENVIADA", notificacion.getEstado());
        assertEquals(TestFixtures.AHORA, notificacion.getFechaEnvio());
        assertEquals("user@example.com", notificacion.getDestinatario());
        assertEquals("Asunto", notificacion.getAsunto());
        assertTrue(notificacion.getMensaje().contains("Mensaje"));

        verify(correoGateway).enviar(
                eq("user@example.com"), eq("Asunto"), contains("<html"));
        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "EXITOSO".equals(historial.getResultado())));
        verifyNoInteractions(authGateway);
    }

    @Test
    void enviarCorreo_resuelveDestinatarioDesdeAuth() {
        when(authGateway.buscarUsuario("u1")).thenReturn(TestFixtures.contacto("u1"));
        prepararGuardado();

        Notificacion notificacion = useCase.enviarCorreo(
                "u1", null, "Asunto", "Mensaje", "GENERICA");

        assertEquals("u1@example.com", notificacion.getDestinatario());
        assertEquals("ENVIADA", notificacion.getEstado());
        verify(authGateway).buscarUsuario("u1");
    }

    @Test
    void enviarCorreo_usaPlantillaActiva() {
        when(plantillaGateway.buscarActivaPorTipo("GENERICA"))
                .thenReturn(TestFixtures.plantilla("pl1", "GENERICA"));
        prepararGuardado();

        Notificacion notificacion = useCase.enviarCorreo(
                "u1", "user@example.com", null, "Hola <mundo>", "GENERICA");

        assertEquals("Asunto u1", notificacion.getAsunto());
        assertTrue(notificacion.getMensaje().contains("&lt;mundo&gt;"));
        assertTrue(notificacion.getMensaje().contains("<html"));
    }

    @Test
    void enviarCorreo_rechazaUsuarioVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.enviarCorreo(
                        "", "user@example.com", "Asunto", "Mensaje", "GENERICA"));

        verifyNoInteractions(notificacionGateway, correoGateway);
    }

    @Test
    void enviarCorreo_fallaSiNoResuelveDestinatario() {
        when(authGateway.buscarUsuario("u1")).thenReturn(null);
        prepararGuardado();

        assertThrows(DestinatarioInvalidoException.class,
                () -> useCase.enviarCorreo(
                        "u1", null, "Asunto", "Mensaje", "GENERICA"));

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionGateway).actualizarNotificacion(captor.capture());
        assertEquals("FALLIDA", captor.getValue().getEstado());
        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
        verifyNoInteractions(correoGateway);
    }

    @Test
    void enviarCorreo_fallaSiAuthNoResponde() {
        when(authGateway.buscarUsuario("u1"))
                .thenThrow(new ServicioExternoNoDisponibleException("auth"));
        prepararGuardado();

        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> useCase.enviarCorreo(
                        "u1", null, "Asunto", "Mensaje", "GENERICA"));

        verify(notificacionGateway).actualizarNotificacion(argThat(notificacion ->
                "FALLIDA".equals(notificacion.getEstado())));
        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
        verifyNoInteractions(correoGateway);
    }

    @Test
    void enviarCorreo_registraFalloSMTP() {
        prepararGuardado();
        doThrow(new EnvioCorreoException("smtp"))
                .when(correoGateway).enviar(any(), any(), any());

        assertThrows(EnvioCorreoException.class,
                () -> useCase.enviarCorreo(
                        "u1", "user@example.com", "Asunto", "Mensaje", "GENERICA"));

        verify(notificacionGateway).actualizarNotificacion(argThat(notificacion ->
                "FALLIDA".equals(notificacion.getEstado())));
        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
    }

    @Test
    void enviarCorreo_registraDestinatarioInvalido() {
        prepararGuardado();
        doThrow(new DestinatarioInvalidoException("correo"))
                .when(correoGateway).enviar(any(), any(), any());

        assertThrows(DestinatarioInvalidoException.class,
                () -> useCase.enviarCorreo(
                        "u1", "correo-invalido", "Asunto", "Mensaje", "GENERICA"));

        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
    }

    @Test
    void enviarCorreo_registraErrorInesperado() {
        prepararGuardado();
        doThrow(new RuntimeException("boom"))
                .when(correoGateway).enviar(any(), any(), any());

        assertThrows(EnvioCorreoException.class,
                () -> useCase.enviarCorreo(
                        "u1", "user@example.com", "Asunto", "Mensaje", "GENERICA"));

        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
    }

    @Test
    void enviarEvento_mapeaTipoYReferencia() {
        when(authGateway.buscarUsuario("u1")).thenReturn(TestFixtures.contacto("u1"));
        prepararGuardado();

        Notificacion notificacion = useCase.enviarEvento(new EventoNotificacion(
                "PRESTAMO_REGISTRADO",
                "u1",
                "p1",
                "Prestamo creado",
                TestFixtures.AHORA
        ));

        assertEquals("PRESTAMO_REGISTRADO", notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("Prestamo creado"));
        assertTrue(notificacion.getMensaje().contains("Referencia: p1"));
        assertEquals("ENVIADA", notificacion.getEstado());
    }

    @Test
    void enviarEvento_rechazaEventoNulo() {
        assertThrows(IllegalArgumentException.class, () -> useCase.enviarEvento(null));
    }

    @Test
    void enviarPrestamoConfirmado_usaMensajePorDefecto() {
        prepararGuardado();

        Notificacion notificacion = useCase.enviarPrestamoConfirmado(
                "u1", "p1", "user@example.com", null);

        assertEquals("PRESTAMO_CONFIRMADO", notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("p1"));
        assertEquals("ENVIADA", notificacion.getEstado());
    }

    @Test
    void enviarLibroDisponible_usaMensajePorDefecto() {
        prepararGuardado();

        Notificacion notificacion = useCase.enviarLibroDisponible(
                "u1", "l1", "user@example.com", null);

        assertEquals("LIBRO_DISPONIBLE", notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("l1"));
    }

    @Test
    void enviarAvisoVencido_usaMensajePorDefecto() {
        prepararGuardado();

        Notificacion notificacion = useCase.enviarAvisoVencido(
                "u1", "p1", "user@example.com", null);

        assertEquals("PRESTAMO_VENCIDO", notificacion.getTipo());
        assertTrue(notificacion.getMensaje().contains("vencido"));
    }

    @Test
    void reintentarNotificacion_rechazaYaEnviada() {
        Notificacion notificacion = new Notificacion(
                "n1", "u1", "user@example.com", "Asunto", "<p>m</p>", "GENERICA",
                TestFixtures.AHORA.minusHours(1), TestFixtures.AHORA.minusMinutes(5), "ENVIADA");
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.reintentarNotificacion("n1"));

        verify(historialEnvioGateway, never()).contarPorNotificacion(any());
    }

    @Test
    void reintentarNotificacion_rechazaMaximoDeIntentos() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", "user@example.com");
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(3L);

        assertThrows(LimiteReintentosExcedidoException.class,
                () -> useCase.reintentarNotificacion("n1"));

        verifyNoInteractions(correoGateway);
    }

    @Test
    void reintentarNotificacion_exitoso() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", "user@example.com");
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion reintentada = useCase.reintentarNotificacion("n1");

        assertEquals("ENVIADA", reintentada.getEstado());
        verify(correoGateway).enviar(
                eq("user@example.com"), eq("Asunto de prueba"), any());
        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "EXITOSO".equals(historial.getResultado())));
    }

    @Test
    void reintentarNotificacion_resuelveDestinatario() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", null);
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(authGateway.buscarUsuario("u1")).thenReturn(TestFixtures.contacto("u1"));
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion reintentada = useCase.reintentarNotificacion("n1");

        assertEquals("u1@example.com", reintentada.getDestinatario());
        assertEquals("ENVIADA", reintentada.getEstado());
    }

    @Test
    void reintentarNotificacion_fallaSiAuthNoResponde() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", null);
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(authGateway.buscarUsuario("u1"))
                .thenThrow(new ServicioExternoNoDisponibleException("auth"));
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> useCase.reintentarNotificacion("n1"));

        verify(historialEnvioGateway).guardarHistorial(argThat(historial ->
                "FALLIDO".equals(historial.getResultado())));
    }

    @Test
    void reintentarNotificacion_fallaSiNoResuelveDestinatario() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", null);
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(authGateway.buscarUsuario("u1")).thenReturn(null);
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(DestinatarioInvalidoException.class,
                () -> useCase.reintentarNotificacion("n1"));
    }

    @Test
    void reintentarNotificacion_noEncontrada() {
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(null);

        assertThrows(NotificacionNoEncontradaException.class,
                () -> useCase.reintentarNotificacion("n1"));
    }

    @Test
    void reintentarFallidas_reintentaLasDisponibles() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", "user@example.com");
        when(notificacionGateway.listarPorEstado("FALLIDA"))
                .thenReturn(List.of(notificacion));
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Notificacion> procesadas = useCase.reintentarFallidas();

        assertEquals(1, procesadas.size());
        assertEquals("ENVIADA", procesadas.get(0).getEstado());
    }

    @Test
    void reintentarFallidas_omiteLasQueAlcanzaronElMaximo() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", "user@example.com");
        when(notificacionGateway.listarPorEstado("FALLIDA"))
                .thenReturn(List.of(notificacion));
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(3L);

        List<Notificacion> procesadas = useCase.reintentarFallidas();

        assertTrue(procesadas.isEmpty());
        verify(notificacionGateway, never()).buscarNotificacionPorId(any());
    }

    @Test
    void reintentarFallidas_continuaCuandoUnReintentoFalla() {
        Notificacion notificacion = TestFixtures.notificacion(
                "n1", "u1", "GENERICA", "FALLIDA", "user@example.com");
        when(notificacionGateway.listarPorEstado("FALLIDA"))
                .thenReturn(List.of(notificacion));
        when(notificacionGateway.buscarNotificacionPorId("n1")).thenReturn(notificacion);
        when(historialEnvioGateway.contarPorNotificacion("n1")).thenReturn(1L);
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new EnvioCorreoException("smtp"))
                .when(correoGateway).enviar(any(), any(), any());

        List<Notificacion> procesadas = useCase.reintentarFallidas();

        assertEquals(1, procesadas.size());
    }

    @Test
    void consultarHistorialUsuario_validaUsuario() {
        when(notificacionGateway.listarPorUsuario("u1"))
                .thenReturn(List.of(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "ENVIADA", "user@example.com")));

        assertEquals(1, useCase.consultarHistorialUsuario("u1").size());
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarHistorialUsuario(""));
    }

    @Test
    void consultarPendientes_listaEstadosNoEnviados() {
        when(notificacionGateway.listarPorEstados(List.of("PENDIENTE", "FALLIDA")))
                .thenReturn(List.of(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "FALLIDA", "user@example.com")));

        assertEquals(1, useCase.consultarPendientes().size());
    }

    @Test
    void listarNotificaciones_yBuscarNotificacion() {
        when(notificacionGateway.listarNotificaciones())
                .thenReturn(List.of(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "ENVIADA", "user@example.com")));
        when(notificacionGateway.buscarNotificacionPorId("n1"))
                .thenReturn(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "ENVIADA", "user@example.com"));

        assertEquals(1, useCase.listarNotificaciones().size());
        assertEquals("n1", useCase.buscarNotificacion("n1").getIdNotificacion());
        assertThrows(NotificacionNoEncontradaException.class,
                () -> useCase.buscarNotificacion("n2"));
    }

    @Test
    void consultarHistorialEnvios_validaNotificacion() {
        HistorialEnvio historial = TestFixtures.historial("h1", "n1", "EXITOSO");
        when(notificacionGateway.buscarNotificacionPorId("n1"))
                .thenReturn(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "ENVIADA", "user@example.com"));
        when(historialEnvioGateway.listarPorNotificacion("n1"))
                .thenReturn(List.of(historial));

        assertEquals(1, useCase.consultarHistorialEnvios("n1").size());
        assertThrows(NotificacionNoEncontradaException.class,
                () -> useCase.consultarHistorialEnvios("n2"));
    }

    @Test
    void eliminarNotificacion_eliminaHistorialYNotificacion() {
        when(notificacionGateway.buscarNotificacionPorId("n1"))
                .thenReturn(TestFixtures.notificacion(
                        "n1", "u1", "GENERICA", "ENVIADA", "user@example.com"));

        useCase.eliminarNotificacion("n1");

        verify(historialEnvioGateway).eliminarPorNotificacion("n1");
        verify(notificacionGateway).eliminarNotificacion("n1");
        assertThrows(NotificacionNoEncontradaException.class,
                () -> useCase.eliminarNotificacion("n2"));
    }

    private void prepararGuardado() {
        when(notificacionGateway.guardarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionGateway.actualizarNotificacion(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
