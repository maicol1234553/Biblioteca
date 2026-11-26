package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DevolverPrestamoRequestTest {

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

    @ParameterizedTest
    @ValueSource(strings = {
            "BUENO", "bueno",
            "DANADO", "danado",
            "DAÑADO", "dañado",
            "PERDIDO", "perdido",
            "BUEN_ESTADO", "buen_estado"
    })
    void validar_cuandoEstadoEsPermitido_noDebeTenerViolaciones(String estado) {
        // Arrange
        DevolverPrestamoRequest request =
                new DevolverPrestamoRequest(estado, null);

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoEstadoEstaEnBlanco_debeIndicarQueEsObligatorio() {
        // Arrange
        DevolverPrestamoRequest request =
                new DevolverPrestamoRequest(" ", null);

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("estadoEjemplar")
                        && violacion.getMessage().equals(
                        "La condicion del ejemplar es obligatoria"
                )
        ));
    }

    @Test
    void validar_cuandoEstadoNoEstaPermitido_debeIndicarFormatoInvalido() {
        // Arrange
        DevolverPrestamoRequest request =
                new DevolverPrestamoRequest("REGULAR", null);

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("estadoEjemplar")
                        && violacion.getMessage().equals(
                        "La condicion debe ser BUENO, DANADO o PERDIDO"
                )
        ));
    }

    @Test
    void validar_cuandoObservacionesTienen1000Caracteres_debeAceptarlas() {
        // Arrange
        DevolverPrestamoRequest request = new DevolverPrestamoRequest(
                "BUENO",
                "a".repeat(1000)
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoObservacionesSuperan1000Caracteres_debeRechazarlas() {
        // Arrange
        DevolverPrestamoRequest request = new DevolverPrestamoRequest(
                "BUENO",
                "a".repeat(1001)
        );

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("observaciones")
                        && violacion.getMessage().equals(
                        "Las observaciones no pueden superar 1000 caracteres"
                )
        ));
    }
}