package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservarLibroRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

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
    void validar_cuandoIdUsuarioEsUuidValido_noDebeTenerViolaciones() {
        // Arrange
        ReservarLibroRequest request = new ReservarLibroRequest(
                "123e4567-e89b-12d3-a456-426614174000"
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    void validar_cuandoIdUsuarioEsNullOVacio_debeIndicarQueEsObligatorio(
            String idUsuario
    ) {
        // Arrange
        ReservarLibroRequest request = new ReservarLibroRequest(idUsuario);

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idUsuario")
                        && violacion.getMessage().equals(
                        "El id del usuario es obligatorio"
                )
        ));
    }

    @Test
    void validar_cuandoIdUsuarioNoEsUuid_debeIndicarFormatoInvalido() {
        // Arrange
        ReservarLibroRequest request =
                new ReservarLibroRequest("usuario-invalido");

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("idUsuario")
                        && violacion.getMessage().equals(
                        "El id del usuario debe ser un UUID valido"
                )
        ));
    }
}