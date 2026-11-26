package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordatorioDevolucionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private static final String UUID_USUARIO =
            "123e4567-e89b-12d3-a456-426614174000";
    private static final String UUID_PRESTAMO =
            "550e8400-e29b-41d4-a716-446655440000";

    @BeforeAll
    static void crearValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidatorFactory() {
        factory.close();
    }

    @Test
    void validar_cuandoFechaEsFuturaYDatosSonValidos_noDebeTenerViolaciones() {

        RecordatorioDevolucionRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                LocalDateTime.now().plusDays(1),
                "DEVOLUCION"
        );


        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        RecordatorioDevolucionRequest request = crearRequest(
                " ",
                UUID_PRESTAMO,
                LocalDateTime.now().plusDays(1),
                "DEVOLUCION"
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdUsuarioNoEsUuid_debeIndicarFormatoInvalido() {
        RecordatorioDevolucionRequest request = crearRequest(
                "usuario-invalido",
                UUID_PRESTAMO,
                LocalDateTime.now().plusDays(1),
                "DEVOLUCION"
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoIdPrestamoEstaVacio_debeIndicarQueEsObligatorio() {
        RecordatorioDevolucionRequest request = crearRequest(
                UUID_USUARIO,
                "",
                LocalDateTime.now().plusDays(1),
                "DEVOLUCION"
        );

        assertTrue(tieneViolacion(
                request,
                "idPrestamo",
                "El id del prestamo es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdPrestamoNoEsUuid_debeIndicarFormatoInvalido() {
        RecordatorioDevolucionRequest request = crearRequest(
                UUID_USUARIO,
                "prestamo-invalido",
                LocalDateTime.now().plusDays(1),
                "DEVOLUCION"
        );

        assertTrue(tieneViolacion(
                request,
                "idPrestamo",
                "El id del prestamo debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoFechaEsPasada_debeIndicarQueDebeSerFutura() {
        RecordatorioDevolucionRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                LocalDateTime.now().minusDays(1),
                "DEVOLUCION"
        );

        assertTrue(tieneViolacion(
                request,
                "fechaProgramada",
                "La fecha programada debe ser futura"
        ));
    }

    @Test
    void validar_cuandoFechaEsNull_noDebeRechazarla() {
        RecordatorioDevolucionRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                null,
                null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    private RecordatorioDevolucionRequest crearRequest(
            String idUsuario,
            String idPrestamo,
            LocalDateTime fechaProgramada,
            String tipo
    ) {
        return new RecordatorioDevolucionRequest(
                idUsuario,
                idPrestamo,
                fechaProgramada,
                tipo
        );
    }

    private boolean tieneViolacion(
            RecordatorioDevolucionRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}