package com.biblioteca.prestamos;

import com.biblioteca.prestamos.domain.model.EjemplarExterno;
import com.biblioteca.prestamos.domain.model.EstadoMulta;
import com.biblioteca.prestamos.domain.model.EstadoReserva;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.PoliticaPrestamos;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class TestFixtures {

    public static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 1, 15, 10, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    public static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);
    public static final PoliticaPrestamos POLITICA =
            new PoliticaPrestamos(3, 15, 3, 2, new BigDecimal("2000.00"));

    private TestFixtures() {
    }

    public static UsuarioExterno usuario(String id) {
        return new UsuarioExterno(id, "Usuario " + id, "USUARIO");
    }

    public static UsuarioExterno admin(String id) {
        return new UsuarioExterno(id, "Admin " + id, "ADMINISTRADOR");
    }

    public static Prestamo prestamo(String id,
                                    String idUsuario,
                                    String idEjemplar,
                                    LocalDateTime fechaVencimiento,
                                    String estado) {
        return new Prestamo(
                id,
                idUsuario,
                idEjemplar,
                AHORA.minusDays(10),
                fechaVencimiento,
                null,
                estado
        );
    }

    public static Reserva reserva(String id,
                                  String idUsuario,
                                  String idLibro,
                                  String estado) {
        return new Reserva(
                id,
                idUsuario,
                idLibro,
                AHORA.minusDays(1),
                AHORA.plusDays(3),
                estado
        );
    }

    public static Multa multa(String id, String idPrestamo, String estado) {
        return new Multa(
                id,
                idPrestamo,
                new BigDecimal("2000.00"),
                "Multa de prueba",
                estado,
                AHORA
        );
    }

    public static Multa multaPendiente(String id, String idPrestamo) {
        return multa(id, idPrestamo, EstadoMulta.PENDIENTE.name());
    }

    public static EjemplarExterno ejemplar(String id, String idLibro, String estado) {
        return new EjemplarExterno(id, idLibro, "Libro de prueba", "INV-001", "A-1", estado);
    }

    public static Reserva reservaPendiente(String id, String idUsuario, String idLibro) {
        return reserva(id, idUsuario, idLibro, EstadoReserva.PENDIENTE.name());
    }
}
