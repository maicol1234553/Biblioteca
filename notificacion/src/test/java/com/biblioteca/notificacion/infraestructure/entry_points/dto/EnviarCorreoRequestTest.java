package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnviarCorreoRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private static final String UUID_VALIDO =
            "123e4567-e89b-12d3-a456-426614174000";

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
    void validar_cuandoDatosSonValidos_noDebeTenerViolaciones() {
        // Arrange
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                "persona@correo.com",
                "Aviso importante",
                "Contenido del mensaje",
                "PRESTAMO"
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        EnviarCorreoRequest request = crearRequest(
                " ",
                "persona@correo.com",
                "Asunto",
                "Mensaje",
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
        EnviarCorreoRequest request = crearRequest(
                "usuario-invalido",
                "persona@correo.com",
                "Asunto",
                "Mensaje",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoDestinatarioNoEsCorreo_debeIndicarFormatoInvalido() {
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                "correo-invalido",
                "Asunto",
                "Mensaje",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "destinatario",
                "El destinatario debe ser un correo valido"
        ));
    }

    @Test
    void validar_cuandoDestinatarioSupera320Caracteres_debeIndicarLimite() {
        // La dirección tiene más de 320 caracteres.
        String destinatario = "a".repeat(310) + "@example.com";

        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                destinatario,
                "Asunto",
                "Mensaje",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "destinatario",
                "El destinatario no puede superar 320 caracteres"
        ));
    }

    @Test
    void validar_cuandoAsuntoEstaVacio_debeIndicarQueEsObligatorio() {
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                null,
                " ",
                "Mensaje",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "asunto",
                "El asunto es obligatorio"
        ));
    }

    @Test
    void validar_cuandoAsuntoSupera255Caracteres_debeIndicarLimite() {
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                null,
                "a".repeat(256),
                "Mensaje",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "asunto",
                "El asunto no puede superar 255 caracteres"
        ));
    }

    @Test
    void validar_cuandoMensajeEstaVacio_debeIndicarQueEsObligatorio() {
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                null,
                "Asunto",
                " ",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "mensaje",
                "El mensaje es obligatorio"
        ));
    }

    @Test
    void validar_cuandoDestinatarioYTipoSonNull_noDebeRechazarlos() {
        EnviarCorreoRequest request = crearRequest(
                UUID_VALIDO,
                null,
                "Asunto",
                "Mensaje",
                null
        );

        assertFalse(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("destinatario")
                        || violacion.getPropertyPath().toString().equals("tipo")
        ));
    }

    private EnviarCorreoRequest crearRequest(
            String idUsuario,
            String destinatario,
            String asunto,
            String mensaje,
            String tipo
    ) {
        return new EnviarCorreoRequest(
                idUsuario,
                destinatario,
                asunto,
                mensaje,
                tipo
        );
    }

    private boolean tieneViolacion(
            EnviarCorreoRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}