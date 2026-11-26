package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvisoVencidoRequestTest {

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
    void validar_cuandoTodosLosDatosSonCorrectos_noDebeTenerViolaciones() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                "persona@correo.com",
                "El préstamo está vencido"
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                " ",
                UUID_PRESTAMO,
                "persona@correo.com",
                "Aviso"
        );

        // Act
        var violaciones = validator.validate(request);

        // Assert
        assertTrue(violaciones.stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idUsuario")
                        && violacion.getMessage().equals(
                        "El id del usuario es obligatorio"
                )
        ));
    }

    @Test
    void validar_cuandoIdUsuarioNoEsUuid_debeIndicarFormatoInvalido() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                "usuario-invalido",
                UUID_PRESTAMO,
                "persona@correo.com",
                "Aviso"
        );

        // Act
        var violaciones = validator.validate(request);

        // Assert
        assertTrue(violaciones.stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idUsuario")
                        && violacion.getMessage().equals(
                        "El id del usuario debe ser un UUID valido"
                )
        ));
    }

    @Test
    void validar_cuandoIdPrestamoEstaVacio_debeIndicarQueEsObligatorio() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                UUID_USUARIO,
                "",
                "persona@correo.com",
                "Aviso"
        );

        // Act
        var violaciones = validator.validate(request);

        // Assert
        assertTrue(violaciones.stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idPrestamo")
                        && violacion.getMessage().equals(
                        "El id del prestamo es obligatorio"
                )
        ));
    }

    @Test
    void validar_cuandoIdPrestamoNoEsUuid_debeIndicarFormatoInvalido() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                UUID_USUARIO,
                "prestamo-invalido",
                "persona@correo.com",
                "Aviso"
        );

        // Act
        var violaciones = validator.validate(request);

        // Assert
        assertTrue(violaciones.stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idPrestamo")
                        && violacion.getMessage().equals(
                        "El id del prestamo debe ser un UUID valido"
                )
        ));
    }

    @Test
    void validar_cuandoDestinatarioNoEsCorreo_debeIndicarFormatoInvalido() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                "correo-invalido",
                "Aviso"
        );

        // Act
        var violaciones = validator.validate(request);

        // Assert
        assertTrue(violaciones.stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("destinatario")
                        && violacion.getMessage().equals(
                        "El destinatario debe ser un correo valido"
                )
        ));
    }

    @Test
    void validar_cuandoDestinatarioYMensajeSonNull_noDebeTenerViolaciones() {
        // Arrange
        AvisoVencidoRequest request = new AvisoVencidoRequest(
                UUID_USUARIO,
                UUID_PRESTAMO,
                null,
                null
        );

        // Act y Assert
        assertFalse(validator.validate(request).stream()
                .anyMatch(violacion ->
                        violacion.getPropertyPath().toString().equals("destinatario")
                ));
    }
}