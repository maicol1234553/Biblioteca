package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestamoConfirmadoRequestTest {

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
    void validar_cuandoDatosSonCorrectos_noDebeTenerViolaciones() {
        PrestamoConfirmadoRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                "persona@correo.com",
                "Tu préstamo fue confirmado"
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        PrestamoConfirmadoRequest request = crearRequest(
                " ",
                UUID_PRESTAMO,
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdUsuarioNoEsUuid_debeIndicarFormatoInvalido() {
        PrestamoConfirmadoRequest request = crearRequest(
                "usuario-invalido",
                UUID_PRESTAMO,
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoIdPrestamoEstaVacio_debeIndicarQueEsObligatorio() {
        PrestamoConfirmadoRequest request = crearRequest(
                UUID_USUARIO,
                "",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idPrestamo",
                "El id del prestamo es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdPrestamoNoEsUuid_debeIndicarFormatoInvalido() {
        PrestamoConfirmadoRequest request = crearRequest(
                UUID_USUARIO,
                "prestamo-invalido",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idPrestamo",
                "El id del prestamo debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoDestinatarioNoEsCorreo_debeIndicarFormatoInvalido() {
        PrestamoConfirmadoRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                "correo-invalido",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "destinatario",
                "El destinatario debe ser un correo valido"
        ));
    }

    @Test
    void validar_cuandoDestinatarioYMensajeSonNull_noDebeTenerViolaciones() {
        PrestamoConfirmadoRequest request = crearRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                null,
                null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    private PrestamoConfirmadoRequest crearRequest(
            String idUsuario,
            String idPrestamo,
            String destinatario,
            String mensaje
    ) {
        return new PrestamoConfirmadoRequest(
                idUsuario,
                idPrestamo,
                destinatario,
                mensaje
        );
    }

    private boolean tieneViolacion(
            PrestamoConfirmadoRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}