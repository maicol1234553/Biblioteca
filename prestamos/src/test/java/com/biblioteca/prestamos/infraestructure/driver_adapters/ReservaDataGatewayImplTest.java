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
        // Arrange
        Reserva reserva = mock(Reserva.class);
        ReservaData reservaData = mock(ReservaData.class);
        ReservaData reservaDataGuardada = mock(ReservaData.class);
        Reserva reservaConvertida = mock(Reserva.class);

        when(reservaMapper.toReservaData(reserva)).thenReturn(reservaData);
        when(reservaDataJpaRepository.save(reservaData)).thenReturn(reservaDataGuardada);
        when(reservaMapper.toReserva(reservaDataGuardada)).thenReturn(reservaConvertida);

        // Act
        Reserva resultado = reservaDataGateway.guardarReserva(reserva);

        // Assert
        assertSame(reservaConvertida, resultado);
        verify(reservaMapper).toReservaData(reserva);
        verify(reservaDataJpaRepository).save(reservaData);
        verify(reservaMapper).toReserva(reservaDataGuardada);
    }

    @Test
    void actualizarReserva_debeGuardarYRetornarReservaActualizada() {
        // Arrange
        Reserva reserva = mock(Reserva.class);
        ReservaData reservaData = mock(ReservaData.class);
        ReservaData reservaDataActualizada = mock(ReservaData.class);
        Reserva reservaActualizada = mock(Reserva.class);

        when(reservaMapper.toReservaData(reserva)).thenReturn(reservaData);
        when(reservaDataJpaRepository.save(reservaData)).thenReturn(reservaDataActualizada);
        when(reservaMapper.toReserva(reservaDataActualizada)).thenReturn(reservaActualizada);

        // Act
        Reserva resultado = reservaDataGateway.actualizarReserva(reserva);

        // Assert
        assertSame(reservaActualizada, resultado);
        verify(reservaMapper).toReservaData(reserva);
        verify(reservaDataJpaRepository).save(reservaData);
        verify(reservaMapper).toReserva(reservaDataActualizada);
    }

    @Test
    void buscarReservaPorId_cuandoExiste_debeRetornarReservaMapeada() {
        // Arrange
        String idReserva = "reserva-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findById(idReserva))
                .thenReturn(Optional.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        Reserva resultado = reservaDataGateway.buscarReservaPorId(idReserva);

        // Assert
        assertSame(reserva, resultado);
        verify(reservaDataJpaRepository).findById(idReserva);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idReserva = "reserva-inexistente";

        when(reservaDataJpaRepository.findById(idReserva))
                .thenReturn(Optional.empty());

        // Act
        Reserva resultado = reservaDataGateway.buscarReservaPorId(idReserva);

        // Assert
        assertNull(resultado);
        verify(reservaDataJpaRepository).findById(idReserva);
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void listarReservas_debeMapearYRetornarLaLista() {
        // Arrange
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findAllByOrderByFechaReservaDesc())
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        List<Reserva> resultado = reservaDataGateway.listarReservas();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findAllByOrderByFechaReservaDesc();
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPorUsuario_debeConsultarYMapearLaLista() {
        // Arrange
        String idUsuario = "usuario-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdUsuarioOrderByFechaReservaDesc(idUsuario))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        List<Reserva> resultado = reservaDataGateway.listarReservasPorUsuario(idUsuario);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdUsuarioOrderByFechaReservaDesc(idUsuario);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPorLibro_debeConsultarYMapearLaLista() {
        // Arrange
        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdLibroOrderByFechaReservaAsc(idLibro))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        List<Reserva> resultado = reservaDataGateway.listarReservasPorLibro(idLibro);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdLibroOrderByFechaReservaAsc(idLibro);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void listarReservasPendientesPorLibro_debeFiltrarPorEstadoPendiente() {
        // Arrange
        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository.findByIdLibroAndEstadoOrderByFechaReservaAsc(
                idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        List<Reserva> resultado =
                reservaDataGateway.listarReservasPendientesPorLibro(idLibro);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository).findByIdLibroAndEstadoOrderByFechaReservaAsc(
                idLibro, EstadoReserva.PENDIENTE.name());
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPendiente_cuandoExiste_debeRetornarReservaMapeada() {
        // Arrange
        String idUsuario = "usuario-1";
        String idLibro = "libro-1";
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(Optional.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        Reserva resultado = reservaDataGateway.buscarReservaPendiente(idUsuario, idLibro);

        // Assert
        assertSame(reserva, resultado);
        verify(reservaDataJpaRepository)
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void buscarReservaPendiente_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(Optional.empty());

        // Act
        Reserva resultado = reservaDataGateway.buscarReservaPendiente(idUsuario, idLibro);

        // Assert
        assertNull(resultado);
        verify(reservaDataJpaRepository)
                .findFirstByIdUsuarioAndIdLibroAndEstadoOrderByFechaReservaAsc(
                        idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void existeReservaPendiente_cuandoExiste_debeRetornarTrue() {
        // Arrange
        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository.existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(true);

        // Act
        boolean resultado =
                reservaDataGateway.existeReservaPendiente(idUsuario, idLibro);

        // Assert
        assertTrue(resultado);
        verify(reservaDataJpaRepository).existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void existeReservaPendiente_cuandoNoExiste_debeRetornarFalse() {
        // Arrange
        String idUsuario = "usuario-1";
        String idLibro = "libro-1";

        when(reservaDataJpaRepository.existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name()))
                .thenReturn(false);

        // Act
        boolean resultado =
                reservaDataGateway.existeReservaPendiente(idUsuario, idLibro);

        // Assert
        assertFalse(resultado);
        verify(reservaDataJpaRepository).existsByIdUsuarioAndIdLibroAndEstado(
                idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void listarReservasPendientesExpiradas_debeFiltrarPorEstadoYFecha() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 25, 12, 0);
        ReservaData reservaData = mock(ReservaData.class);
        Reserva reserva = mock(Reserva.class);

        when(reservaDataJpaRepository
                .findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
                        EstadoReserva.PENDIENTE.name(), fecha))
                .thenReturn(List.of(reservaData));
        when(reservaMapper.toReserva(reservaData)).thenReturn(reserva);

        // Act
        List<Reserva> resultado =
                reservaDataGateway.listarReservasPendientesExpiradas(fecha);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(reserva, resultado.get(0));
        verify(reservaDataJpaRepository)
                .findByEstadoAndFechaExpiracionBeforeOrderByFechaExpiracionAsc(
                        EstadoReserva.PENDIENTE.name(), fecha);
        verify(reservaMapper).toReserva(reservaData);
    }

    @Test
    void eliminarReserva_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idReserva = "reserva-1";

        // Act
        reservaDataGateway.eliminarReserva(idReserva);

        // Assert
        verify(reservaDataJpaRepository).deleteById(idReserva);
        verify(reservaDataJpaRepository).flush();
        verifyNoInteractions(reservaMapper);
    }

    @Test
    void eliminarReserva_cuandoTieneInformacionAsociada_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idReserva = "reserva-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(reservaDataJpaRepository).flush();

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> reservaDataGateway.eliminarReserva(idReserva)
        );

        // Assert
        assertEquals(
                "No se puede eliminar la reserva porque tiene informacion asociada",
                excepcion.getMessage()
        );
        verify(reservaDataJpaRepository).deleteById(idReserva);
        verify(reservaDataJpaRepository).flush();
    }
}