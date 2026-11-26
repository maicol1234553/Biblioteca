package com.biblioteca.prestamos.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ActualizarVencimientoRequestTest {

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
    void validar_cuandoFechaEsFutura_noDebeTenerViolaciones() {
        // Arrange
        ActualizarVencimientoRequest request =
                new ActualizarVencimientoRequest(LocalDateTime.now().plusDays(1));

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoFechaEsNull_debeIndicarQueEsObligatoria() {
        // Arrange
        ActualizarVencimientoRequest request =
                new ActualizarVencimientoRequest(null);

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("fechaVencimiento")
                        && violacion.getMessage().equals(
                        "La nueva fecha de vencimiento es obligatoria"
                )
        ));
    }

    @Test
    void validar_cuandoFechaEsPasada_debeIndicarQueDebeSerFutura() {
        // Arrange
        ActualizarVencimientoRequest request =
                new ActualizarVencimientoRequest(LocalDateTime.now().minusDays(1));

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("fechaVencimiento")
                        && violacion.getMessage().equals(
                        "La nueva fecha de vencimiento debe ser futura"
                )
        ));
    }

    @Test
    void validar_cuandoFechaEsElMomentoActual_debeIndicarQueDebeSerFutura() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();
        ActualizarVencimientoRequest request =
                new ActualizarVencimientoRequest(ahora);

        // Act y Assert
        assertTrue(validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals("fechaVencimiento")
                        && violacion.getMessage().equals(
                        "La nueva fecha de vencimiento debe ser futura"
                )
        ));
    }
}