package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.TestFixtures;
import com.biblioteca.prestamos.domain.exception.EstadoInvalidoException;
import com.biblioteca.prestamos.domain.exception.MultaNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.gateway.MultaGateway;
import com.biblioteca.prestamos.domain.model.gateway.PrestamoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultaUseCaseTest {

    @Mock
    private MultaGateway multaGateway;
    @Mock
    private PrestamoGateway prestamoGateway;

    private MultaUseCase useCase;
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        useCase = new MultaUseCase(multaGateway, prestamoGateway);
        prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "ACTIVO");
    }

    @Test
    void consultarMultasPendientesPorUsuario_validaPropietario() {
        when(multaGateway.listarMultasPendientesPorUsuario("u1"))
                .thenReturn(List.of(TestFixtures.multaPendiente("m1", "p1")));

        assertEquals(1, useCase.consultarMultasPendientesPorUsuario(
                "u1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarMultasPendientesPorUsuario(
                        "u1", TestFixtures.usuario("u2")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarMultasPendientesPorUsuario(
                        "", TestFixtures.usuario("u1")));
    }

    @Test
    void consultarMultasPorUsuario_validaPropietario() {
        when(multaGateway.listarMultasPorUsuario("u1"))
                .thenReturn(List.of(TestFixtures.multaPendiente("m1", "p1")));

        assertEquals(1, useCase.consultarMultasPorUsuario(
                "u1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.consultarMultasPorUsuario(
                        "u1", TestFixtures.usuario("u2")));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.consultarMultasPorUsuario(
                        "", TestFixtures.usuario("u1")));
    }

    @Test
    void listarMultas_soloAdministradores() {
        when(multaGateway.listarMultas()).thenReturn(List.of());

        assertTrue(useCase.listarMultas(TestFixtures.admin("admin-1")).isEmpty());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarMultas(TestFixtures.usuario("u1")));
    }

    @Test
    void buscarMulta_validaExistenciaYPropietario() {
        Multa multa = TestFixtures.multaPendiente("m1", "p1");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(multa);
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertEquals(multa, useCase.buscarMulta("m1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarMulta("m1", TestFixtures.usuario("u2")));

        when(multaGateway.buscarMultaPorId("m2")).thenReturn(null);
        assertThrows(MultaNoEncontradaException.class,
                () -> useCase.buscarMulta("m2", TestFixtures.usuario("u1")));
    }

    @Test
    void buscarMulta_rechazaPrestamoNoEncontrado() {
        when(multaGateway.buscarMultaPorId("m1"))
                .thenReturn(TestFixtures.multaPendiente("m1", "p1"));
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.buscarMulta("m1", TestFixtures.usuario("u1")));
    }

    @Test
    void listarMultasPorPrestamo_validaPropietario() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(multaGateway.listarMultasPorPrestamo("p1"))
                .thenReturn(List.of(TestFixtures.multaPendiente("m1", "p1")));

        assertEquals(1, useCase.listarMultasPorPrestamo(
                "p1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarMultasPorPrestamo(
                        "p1", TestFixtures.usuario("u2")));

        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(null);
        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.listarMultasPorPrestamo(
                        "p2", TestFixtures.usuario("u1")));
    }

    @Test
    void pagarMulta_cambiaEstado() {
        Multa multa = TestFixtures.multaPendiente("m1", "p1");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(multa);
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(multaGateway.actualizarMulta(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Multa pagada = useCase.pagarMulta("m1", TestFixtures.usuario("u1"));

        assertEquals("PAGADA", pagada.getEstado());
    }

    @Test
    void pagarMulta_rechazaEstadosYUsuariosInvalidos() {
        Multa pagada = TestFixtures.multa("m1", "p1", "PAGADA");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(pagada);
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertThrows(EstadoInvalidoException.class,
                () -> useCase.pagarMulta("m1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.pagarMulta("m1", TestFixtures.usuario("u2")));
    }

    @Test
    void pagarMulta_rechazaMultaOPrestamoNoEncontrados() {
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(null);
        assertThrows(MultaNoEncontradaException.class,
                () -> useCase.pagarMulta("m1", TestFixtures.usuario("u1")));

        when(multaGateway.buscarMultaPorId("m2"))
                .thenReturn(TestFixtures.multaPendiente("m2", "p2"));
        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(null);
        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.pagarMulta("m2", TestFixtures.usuario("u1")));
    }

    @Test
    void anularMulta_soloAdministradores() {
        Multa multa = TestFixtures.multaPendiente("m1", "p1");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(multa);
        when(multaGateway.actualizarMulta(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Multa anulada = useCase.anularMulta("m1", TestFixtures.admin("admin-1"));

        assertEquals("ANULADA", anulada.getEstado());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.anularMulta("m1", TestFixtures.usuario("u1")));
    }

    @Test
    void anularMulta_rechazaEstadosInvalidosYNoEncontradas() {
        Multa pagada = TestFixtures.multa("m1", "p1", "PAGADA");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(pagada);
        assertThrows(EstadoInvalidoException.class,
                () -> useCase.anularMulta("m1", TestFixtures.admin("admin-1")));

        when(multaGateway.buscarMultaPorId("m2")).thenReturn(null);
        assertThrows(MultaNoEncontradaException.class,
                () -> useCase.anularMulta("m2", TestFixtures.admin("admin-1")));
    }

    @Test
    void eliminarMulta_soloAnuladasYAdministradores() {
        Multa anulada = TestFixtures.multa("m1", "p1", "ANULADA");
        when(multaGateway.buscarMultaPorId("m1")).thenReturn(anulada);

        useCase.eliminarMulta("m1", TestFixtures.admin("admin-1"));
        verify(multaGateway).eliminarMulta("m1");

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.eliminarMulta("m1", TestFixtures.usuario("u1")));

        Multa pendiente = TestFixtures.multaPendiente("m2", "p1");
        when(multaGateway.buscarMultaPorId("m2")).thenReturn(pendiente);
        assertThrows(EstadoInvalidoException.class,
                () -> useCase.eliminarMulta("m2", TestFixtures.admin("admin-1")));

        when(multaGateway.buscarMultaPorId("m3")).thenReturn(null);
        assertThrows(MultaNoEncontradaException.class,
                () -> useCase.eliminarMulta("m3", TestFixtures.admin("admin-1")));
    }
}
