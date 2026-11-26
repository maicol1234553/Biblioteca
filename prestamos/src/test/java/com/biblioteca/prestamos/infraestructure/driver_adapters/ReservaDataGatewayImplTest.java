package com.biblioteca.prestamos.infraestructure.driver_adapters;

import com.biblioteca.prestamos.domain.exception.RecursoEnUsoException;
import com.biblioteca.prestamos.domain.model.EstadoReserva;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.infraestructure.mapper.ReservaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaDataGatewayImplTest {

    @Mock
    private ReservaDataJpaRepository reservaDataJpaRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaDataGatewayImpl reservaDataGateway;

    @Test
    void guardarReserva_debeMapearGuardarYRetornarReserva() {

        Reserva reserva = mock(Reserva.class);
        ReservaData reservaData = mock(ReservaData.class);
        ReservaData reservaDataGuardada = mock(ReservaData.class);
        Reserva reservaConvertida = mock(Reserva.class);

        when(reservaMapper.toReservaData(reserva)).thenReturn(reservaData);
        when(reservaDataJpaRepository.save(reservaData)).thenReturn(reservaDataGuardada);
        when(reservaMapper.toReserva(reservaDataGuardada)).thenReturn(reservaConvertida);


        Reserva resultado = reservaDataGateway.guardarReserva(reserva);


        assertSame(reservaConvertida, resultado);
        verify(reservaMapper).toReservaData(reserva);
        verify(reservaDataJpaRepository).save(reservaData);
        verify(reservaMapper).toReserva(reservaDataGuardada);
    }

    @Test
    void actualizarReserva_debeGuardarYRetornarReservaActualizada() {

        Reserva reserva = mock(Reserva.class);
        ReservaData reservaData = mock(ReservaData.class);
        ReservaData reservaDataActualizada = mock(ReservaData.class);
        Reserva reservaActualizada = mock(Reserva.class);

        when(reservaMapper.toReservaData(reserva)).thenReturn(reservaData);
        when(reservaDataJpaRepository.save(reservaData)).thenReturn(reservaDataActualizada);
        when(reservaMapper.toReserva(reservaDataActualizada)).thenReturn(reservaActualizada);


        Reserva resultado = reservaDataGateway.actualizarReserva(reserva);


        assertSame(reservaActualizada, resultado);
        verify(reservaMapper).toReservaData(reserva);
        verify(reservaDataJpaRepository).save(reservaData);
        verify(reservaMapper).toReserva(reservaDataActualizada);
    }

    @Test
    void buscarReservaPorId_cuandoExiste_debeRetornarReservaMapeada() {

        String idReserva = "reserva-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findById(idReserva))
                .thenReturn(Optional.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        Reserva resultado = reservaDataGateway.buscarReservaPorId(idReserva);


        assertSame(reserva, resultado);
        verify(reservaDataJpaRepository).findById(idReserva);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPorId_cuandoNoExiste_debeRetornarNull() {

        String idReserva = "reserva-inexistente";

        when(reservaDataJpaRepository.findById(idReserva))
                .thenReturn(Optional.empty());


        Reserva resultado = reservaDataGateway.buscarReservaPorId(idReserva);


        assertNull(resultado);
        verify(reservaDataJpaRepository).findById(idReserva);
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void listarReservas_debeMapearYRetornarLaLista() {

        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findAllByOrderByFechaReservaDesc())
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        List<Reserva> resultado = reservaDataGateway.listarReservas();


        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findAllByOrderByFechaReservaDesc();
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPorUsuario_debeConsultarYMapearLaLista() {

        String idUsuario = "usuario-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdUsuarioOrderByFechaReservaDesc(idUsuario))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        List<Reserva> resultado = reservaDataGateway.listarReservasPorUsuario(idUsuario);


        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdUsuarioOrderByFechaReservaDesc(idUsuario);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPorLibro_debeConsultarYMapearLaLista() {

        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdLibroOrderByFechaReservaAsc(idLibro))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        List<Reserva> resultado = reservaDataGateway.listarReservasPorLibro(idLibro);


        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdLibroOrderByFechaReservaAsc(idLibro);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPendientesPorLibro_debeFiltrarPorEstadoPendiente() {

        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdLibroAndEstadoOrderByFechaReservaAsc(
                idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        List<Reserva> resultado =
                reservaDataGateway.listarReservasPendientesPorLibro(idLibro);


        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdLibroAndEstadoOrderByFechaReservaAsc(
                idLibro, EstadoReserva.PENDIENTE.name());
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPendiente_cuandoExiste_debeRetornarReservaMapeada() {

        String idUsuario = "usuario-1";
        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(Optional.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        Reserva resultado = reservaDataGateway.buscarReservaPendiente(idUsuario, idLibro);


        assertSame(reserva, resultado);
        verify(reservaDataJpaRepository)
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPendiente_cuandoNoExiste_debeRetornarNull() {

        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(Optional.empty());


        Reserva resultado = reservaDataGateway.buscarReservaPendiente(idUsuario, idLibro);


        assertNull(resultado);
        verify(reservaDataJpaRepository)
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void existeReservaPendiente_cuandoExiste_debeRetornarTrue() {

        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository.existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(true);


        boolean resultado =
                reservaDataGateway.existeReservaPendiente(idUsuario, idLibro);


        assertTrue(resultado);
        verify(reservaDataJpaRepository).existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void existeReservaPendiente_cuandoNoExiste_debeRetornarFalse() {

        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository.existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(false);


        boolean resultado =
                reservaDataGateway.existeReservaPendiente(idUsuario, idLibro);


        assertFalse(resultado);
        verify(reservaDataJpaRepository).existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void listarReservasPendientesExpiradas_debeFiltrarPorEstadoYFecha() {

        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 12, 0);
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository
                .findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
                        EstadoReserva.PENDIENTE.name(), fecha))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);


        List<Reserva> resultado =
                reservaDataGateway.listarReservasPendientesExpiradas(fecha);


        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository)
                .findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
                        EstadoReserva.PENDIENTE.name(), fecha);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void eliminarReserva_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {

        String idReserva = "reserva-1";


        reservaDataGateway.eliminarReserva(idReserva);


        verify(reservaDataJpaRepository).deleteById(idReserva);
        verify(reservaDataJpaRepository).flush();
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void eliminarReserva_cuandoTieneInformacionAsociada_debeLanzarRecursoEnUsoException() {

        String idReserva = "reserva-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(reservaDataJpaRepository).flush();


        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> reservaDataGateway.eliminarReserva(idReserva)
        );


        assertEquals(
                "No se puede eliminar la reserva porque tiene informacion asociada",
                excepcion.getMessage()
        );
        verify(reservaDataJpaRepository).deleteById(idReserva);
        verify(reservaDataJpaRepository).flush();
    }
}