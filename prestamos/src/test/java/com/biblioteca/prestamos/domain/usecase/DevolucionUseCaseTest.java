package com.biblioteca.prestamos.domain.usecase;

import com.biblioteca.prestamos.TestFixtures;
import com.biblioteca.prestamos.domain.exception.DevolucionNoEncontradaException;
import com.biblioteca.prestamos.domain.exception.PrestamoNoEncontradoException;
import com.biblioteca.prestamos.domain.exception.UsuarioNoAutorizadoException;
import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.gateway.DevolucionGateway;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevolucionUseCaseTest {

    @Mock
    private DevolucionGateway devolucionGateway;
    @Mock
    private PrestamoGateway prestamoGateway;

    private DevolucionUseCase useCase;
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        useCase = new DevolucionUseCase(devolucionGateway, prestamoGateway);
        prestamo = TestFixtures.prestamo(
                "p1", "u1", "e1", TestFixtures.AHORA.plusDays(2), "DEVUELTO");
    }

    @Test
    void listarDevoluciones_soloAdministradores() {
        when(devolucionGateway.listarDevoluciones()).thenReturn(List.of(devolucion()));

        assertEquals(1, useCase.listarDevoluciones(TestFixtures.admin("admin-1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarDevoluciones(TestFixtures.usuario("u1")));
    }

    @Test
    void buscarDevolucion_validaExistenciaYPropietario() {
        Devolucion devolucion = devolucion();
        when(devolucionGateway.buscarDevolucionPorId("d1")).thenReturn(devolucion);
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);

        assertEquals(devolucion, useCase.buscarDevolucion(
                "d1", TestFixtures.usuario("u1")));
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarDevolucion("d1", TestFixtures.usuario("u2")));

        when(devolucionGateway.buscarDevolucionPorId("d2")).thenReturn(null);
        assertThrows(DevolucionNoEncontradaException.class,
                () -> useCase.buscarDevolucion("d2", TestFixtures.usuario("u1")));
    }

    @Test
    void buscarDevolucion_rechazaPrestamoNoEncontrado() {
        when(devolucionGateway.buscarDevolucionPorId("d1")).thenReturn(devolucion());
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.buscarDevolucion("d1", TestFixtures.usuario("u1")));
    }

    @Test
    void listarDevolucionesPorPrestamo_validaPropietario() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.listarDevolucionesPorPrestamo("p1"))
                .thenReturn(List.of(devolucion()));

        assertEquals(1, useCase.listarDevolucionesPorPrestamo(
                "p1", TestFixtures.usuario("u1")).size());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.listarDevolucionesPorPrestamo(
                        "p1", TestFixtures.usuario("u2")));

        when(prestamoGateway.buscarPrestamoPorId("p2")).thenReturn(null);
        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.listarDevolucionesPorPrestamo(
                        "p2", TestFixtures.usuario("u1")));
    }

    @Test
    void buscarDevolucionPorPrestamo_validaExistencia() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(prestamo);
        when(devolucionGateway.buscarDevolucionPorPrestamo("p1")).thenReturn(devolucion());

        assertEquals("d1", useCase.buscarDevolucionPorPrestamo(
                "p1", TestFixtures.usuario("u1")).getIdDevolucion());
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.buscarDevolucionPorPrestamo(
                        "p1", TestFixtures.usuario("u2")));

        when(devolucionGateway.buscarDevolucionPorPrestamo("p1")).thenReturn(null);
        assertThrows(DevolucionNoEncontradaException.class,
                () -> useCase.buscarDevolucionPorPrestamo(
                        "p1", TestFixtures.usuario("u1")));
    }

    @Test
    void buscarDevolucionPorPrestamo_rechazaPrestamoNoEncontrado() {
        when(prestamoGateway.buscarPrestamoPorId("p1")).thenReturn(null);

        assertThrows(PrestamoNoEncontradoException.class,
                () -> useCase.buscarDevolucionPorPrestamo(
                        "p1", TestFixtures.usuario("u1")));
    }

    @Test
    void eliminarDevolucion_soloAdministradores() {
        when(devolucionGateway.buscarDevolucionPorId("d1")).thenReturn(devolucion());

        useCase.eliminarDevolucion("d1", TestFixtures.admin("admin-1"));
        verify(devolucionGateway).eliminarDevolucion("d1");

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> useCase.eliminarDevolucion("d1", TestFixtures.usuario("u1")));

        when(devolucionGateway.buscarDevolucionPorId("d2")).thenReturn(null);
        assertThrows(DevolucionNoEncontradaException.class,
                () -> useCase.eliminarDevolucion("d2", TestFixtures.admin("admin-1")));
    }

    private Devolucion devolucion() {
        return new Devolucion(
                "d1", "p1", TestFixtures.AHORA, "Sin novedad", "BUENO");
    }
}
