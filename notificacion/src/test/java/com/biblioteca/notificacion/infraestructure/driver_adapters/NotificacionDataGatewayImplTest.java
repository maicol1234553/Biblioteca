package com.biblioteca.notificacion.infraestructure.driver_adapters;

import com.biblioteca.notificacion.domain.exception.RecursoEnUsoException;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.infraestructure.mapper.NotificacionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionDataGatewayImplTest {

    @Mock
    private NotificacionDataJpaRepository notificacionDataJpaRepository;

    @Mock
    private NotificacionMapper notificacionMapper;

    @InjectMocks
    private NotificacionDataGatewayImpl notificacionDataGateway;

    @Test
    void guardarNotificacion_debeMapearGuardarYRetornarNotificacion() {

        Notificacion notificacion = mock(Notificacion.class);
        NotificacionData notificacionData = mock(NotificacionData.class);
        NotificacionData notificacionDataGuardada = mock(NotificacionData.class);
        Notificacion notificacionConvertida = mock(Notificacion.class);

        when(notificacionMapper.toNotificacionData(notificacion)).thenReturn(notificacionData);
        when(notificacionDataJpaRepository.save(notificacionData))
                .thenReturn(notificacionDataGuardada);
        when(notificacionMapper.toNotificacion(notificacionDataGuardada))
                .thenReturn(notificacionConvertida);


        Notificacion resultado =
                notificacionDataGateway.guardarNotificacion(notificacion);


        assertSame(notificacionConvertida, resultado);
        verify(notificacionMapper).toNotificacionData(notificacion);
        verify(notificacionDataJpaRepository).save(notificacionData);
        verify(notificacionMapper).toNotificacion(notificacionDataGuardada);
    }

    @Test
    void actualizarNotificacion_debeGuardarYRetornarNotificacionActualizada() {

        Notificacion notificacion = mock(Notificacion.class);
        NotificacionData notificacionData = mock(NotificacionData.class);
        NotificacionData notificacionDataActualizada = mock(NotificacionData.class);
        Notificacion notificacionActualizada = mock(Notificacion.class);

        when(notificacionMapper.toNotificacionData(notificacion)).thenReturn(notificacionData);
        when(notificacionDataJpaRepository.save(notificacionData))
                .thenReturn(notificacionDataActualizada);
        when(notificacionMapper.toNotificacion(notificacionDataActualizada))
                .thenReturn(notificacionActualizada);


        Notificacion resultado =
                notificacionDataGateway.actualizarNotificacion(notificacion);


        assertSame(notificacionActualizada, resultado);
        verify(notificacionMapper).toNotificacionData(notificacion);
        verify(notificacionDataJpaRepository).save(notificacionData);
        verify(notificacionMapper).toNotificacion(notificacionDataActualizada);
    }

    @Test
    void buscarNotificacionPorId_cuandoExiste_debeRetornarNotificacionMapeada() {

        String idNotificacion = "notificacion-1";
        NotificacionData notificacionData = mock(NotificacionData.class);
        Notificacion notificacion = mock(Notificacion.class);

        when(notificacionDataJpaRepository.findById(idNotificacion))
                .thenReturn(Optional.of(notificacionData));
        when(notificacionMapper.toNotificacion(notificacionData)).thenReturn(notificacion);


        Notificacion resultado =
                notificacionDataGateway.buscarNotificacionPorId(idNotificacion);


        assertSame(notificacion, resultado);
        verify(notificacionDataJpaRepository).findById(idNotificacion);
        verify(notificacionMapper).toNotificacion(notificacionData);
    }

    @Test
    void buscarNotificacionPorId_cuandoNoExiste_debeRetornarNull() {

        String idNotificacion = "notificacion-inexistente";

        when(notificacionDataJpaRepository.findById(idNotificacion))
                .thenReturn(Optional.empty());


        Notificacion resultado =
                notificacionDataGateway.buscarNotificacionPorId(idNotificacion);


        assertNull(resultado);
        verify(notificacionDataJpaRepository).findById(idNotificacion);
        verifyNoInteractions(notificacionMapper);
    }

    @Test
    void listarNotificaciones_debeMapearYRetornarLaLista() {

        NotificacionData notificacionData = mock(NotificacionData.class);
        Notificacion notificacion = mock(Notificacion.class);

        when(notificacionDataJpaRepository.findAllByOrderByFechaCreacionDesc())
                .thenReturn(List.of(notificacionData));
        when(notificacionMapper.toNotificacion(notificacionData)).thenReturn(notificacion);


        List<Notificacion> resultado = notificacionDataGateway.listarNotificaciones();


        assertEquals(1, resultado.size());
        assertSame(notificacion, resultado.get(0));
        verify(notificacionDataJpaRepository).findAllByOrderByFechaCreacionDesc();
        verify(notificacionMapper).toNotificacion(notificacionData);
    }

    @Test
    void listarPorUsuario_debeConsultarYMapearLasNotificaciones() {

        String idUsuario = "usuario-1";
        NotificacionData notificacionData = mock(NotificacionData.class);
        Notificacion notificacion = mock(Notificacion.class);

        when(notificacionDataJpaRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario))
                .thenReturn(List.of(notificacionData));
        when(notificacionMapper.toNotificacion(notificacionData)).thenReturn(notificacion);


        List<Notificacion> resultado = notificacionDataGateway.listarPorUsuario(idUsuario);


        assertEquals(1, resultado.size());
        assertSame(notificacion, resultado.get(0));
        verify(notificacionDataJpaRepository)
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);
        verify(notificacionMapper).toNotificacion(notificacionData);
    }

    @Test
    void listarPorEstado_debeConsultarYMapearLasNotificaciones() {

        String estado = "FALLIDA";
        NotificacionData notificacionData = mock(NotificacionData.class);
        Notificacion notificacion = mock(Notificacion.class);

        when(notificacionDataJpaRepository.findByEstadoOrderByFechaCreacionDesc(estado))
                .thenReturn(List.of(notificacionData));
        when(notificacionMapper.toNotificacion(notificacionData)).thenReturn(notificacion);


        List<Notificacion> resultado = notificacionDataGateway.listarPorEstado(estado);


        assertEquals(1, resultado.size());
        assertSame(notificacion, resultado.get(0));
        verify(notificacionDataJpaRepository).findByEstadoOrderByFechaCreacionDesc(estado);
        verify(notificacionMapper).toNotificacion(notificacionData);
    }

    @Test
    void listarPorEstados_debeConsultarYMapearLasNotificaciones() {

        Collection<String> estados = List.of("PENDIENTE", "FALLIDA");
        NotificacionData notificacionData = mock(NotificacionData.class);
        Notificacion notificacion = mock(Notificacion.class);

        when(notificacionDataJpaRepository.findByEstadoInOrderByFechaCreacionDesc(estados))
                .thenReturn(List.of(notificacionData));
        when(notificacionMapper.toNotificacion(notificacionData)).thenReturn(notificacion);


        List<Notificacion> resultado = notificacionDataGateway.listarPorEstados(estados);


        assertEquals(1, resultado.size());
        assertSame(notificacion, resultado.get(0));
        verify(notificacionDataJpaRepository)
                .findByEstadoInOrderByFechaCreacionDesc(estados);
        verify(notificacionMapper).toNotificacion(notificacionData);
    }

    @Test
    void eliminarNotificacion_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {

        String idNotificacion = "notificacion-1";


        notificacionDataGateway.eliminarNotificacion(idNotificacion);


        verify(notificacionDataJpaRepository).deleteById(idNotificacion);
        verify(notificacionDataJpaRepository).flush();
        verifyNoInteractions(notificacionMapper);
    }

    @Test
    void eliminarNotificacion_cuandoTieneHistorialAsociado_debeLanzarRecursoEnUsoException() {

        String idNotificacion = "notificacion-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(notificacionDataJpaRepository).flush();


        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> notificacionDataGateway.eliminarNotificacion(idNotificacion)
        );


        assertEquals(
                "No se puede eliminar la notificacion porque tiene historial asociado",
                excepcion.getMessage()
        );
        verify(notificacionDataJpaRepository).deleteById(idNotificacion);
        verify(notificacionDataJpaRepository).flush();
    }
}