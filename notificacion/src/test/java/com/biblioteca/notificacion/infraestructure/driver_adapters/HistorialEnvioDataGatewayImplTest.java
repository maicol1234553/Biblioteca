package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.exception.NotificacionNoEncontradaException;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.infraestructure.mapper.HistorialEnvioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialEnvioDataGatewayImplTest {

    @Mock
    private HistorialEnvioDataJpaRepository historialEnvioDataJpaRepository;

    @Mock
    private NotificacionDataJpaRepository notificacionDataJpaRepository;

    @Mock
    private HistorialEnvioMapper historialEnvioMapper;

    @InjectMocks
    private HistorialEnvioDataGatewayImpl historialEnvioDataGateway;

    @Test
    void guardarHistorial_debeBuscarNotificacionMapearGuardarYRetornarHistorial() {

        HistorialEnvio historial = mock(HistorialEnvio.class);
        NotificacionData notificacionData = mock(NotificacionData.class);
        HistorialEnvioData historialData = mock(HistorialEnvioData.class);
        HistorialEnvioData historialDataGuardado = mock(HistorialEnvioData.class);
        HistorialEnvio historialConvertido = mock(HistorialEnvio.class);

        when(historial.getIdNotificacion()).thenReturn("notificacion-1");
        when(notificacionDataJpaRepository.findById("notificacion-1"))
                .thenReturn(Optional.of(notificacionData));
        when(historialEnvioMapper.toHistorialData(historial, notificacionData))
                .thenReturn(historialData);
        when(historialEnvioDataJpaRepository.save(historialData))
                .thenReturn(historialDataGuardado);
        when(historialEnvioMapper.toHistorial(historialDataGuardado))
                .thenReturn(historialConvertido);


        HistorialEnvio resultado =
                historialEnvioDataGateway.guardarHistorial(historial);


        assertSame(historialConvertido, resultado);
        verify(notificacionDataJpaRepository).findById("notificacion-1");
        verify(historialEnvioMapper).toHistorialData(historial, notificacionData);
        verify(historialEnvioDataJpaRepository).save(historialData);
        verify(historialEnvioMapper).toHistorial(historialDataGuardado);
    }

    @Test
    void guardarHistorial_cuandoNotificacionNoExiste_debeLanzarExcepcion() {

        HistorialEnvio historial = mock(HistorialEnvio.class);

        when(historial.getIdNotificacion()).thenReturn("notificacion-inexistente");
        when(notificacionDataJpaRepository.findById("notificacion-inexistente"))
                .thenReturn(Optional.empty());


        NotificacionNoEncontradaException excepcion = assertThrows(
                NotificacionNoEncontradaException.class,
                () -> historialEnvioDataGateway.guardarHistorial(historial)
        );


        assertEquals(
                "Notificacion no encontrada con id: notificacion-inexistente",
                excepcion.getMessage()
        );
        verify(historialEnvioDataJpaRepository, never()).save(any());
        verifyNoInteractions(historialEnvioMapper);
    }

    @Test
    void listarPorNotificacion_debeMapearYRetornarHistoriales() {

        String idNotificacion = "notificacion-1";
        HistorialEnvioData historialData = mock(HistorialEnvioData.class);
        HistorialEnvio historial = mock(HistorialEnvio.class);

        when(historialEnvioDataJpaRepository
                .findByNotificacion_IdNotificacionOrderByFechaIntentoDesc(idNotificacion))
                .thenReturn(List.of(historialData));
        when(historialEnvioMapper.toHistorial(historialData)).thenReturn(historial);


        List<HistorialEnvio> resultado =
                historialEnvioDataGateway.listarPorNotificacion(idNotificacion);


        assertEquals(1, resultado.size());
        assertSame(historial, resultado.get(0));
        verify(historialEnvioDataJpaRepository)
                .findByNotificacion_IdNotificacionOrderByFechaIntentoDesc(idNotificacion);
        verify(historialEnvioMapper).toHistorial(historialData);
    }

    @Test
    void contarPorNotificacion_debeRetornarCantidadDeEnvios() {

        String idNotificacion = "notificacion-1";
        when(historialEnvioDataJpaRepository
                .countByNotificacion_IdNotificacion(idNotificacion))
                .thenReturn(3L);


        long resultado =
                historialEnvioDataGateway.contarPorNotificacion(idNotificacion);


        assertEquals(3L, resultado);
        verify(historialEnvioDataJpaRepository)
                .countByNotificacion_IdNotificacion(idNotificacion);
        verifyNoInteractions(historialEnvioMapper);
    }

    @Test
    void eliminarPorNotificacion_debeSolicitarEliminacionDeLosEnvios() {

        String idNotificacion = "notificacion-1";


        historialEnvioDataGateway.eliminarPorNotificacion(idNotificacion);


        verify(historialEnvioDataJpaRepository)
                .deleteByNotificacion_IdNotificacion(idNotificacion);
        verifyNoInteractions(notificacionDataJpaRepository, historialEnvioMapper);
    }
}