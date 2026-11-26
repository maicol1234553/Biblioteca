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

class SolicitarPrestamoRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private static final String UUID_USUARIO =
            "123e4567-e89b-12d3-a456-426614174000";
    private static final String UUID_EJEMPLAR =
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
    void validar_cuandoLosIdsSonUuidValidos_noDebeTenerViolaciones() {

        SolicitarPrestamoRequest request =
                new SolicitarPrestamoRequest(UUID_USUARIO, UUID_EJEMPLAR);


        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    void validar_cuandoIdUsuarioEsNullOVacio_debeIndicarQueEsObligatorio(
            String idUsuario
    ) {

        SolicitarPrestamoRequest request =
                new SolicitarPrestamoRequest(idUsuario, UUID_EJEMPLAR);


        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdUsuarioNoEsUuid_debeIndicarFormatoInvalido() {

        SolicitarPrestamoRequest request =
                new SolicitarPrestamoRequest("usuario-invalido", UUID_EJEMPLAR);


        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario debe ser un UUID valido"
        ));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    void validar_cuandoIdEjemplarEsNullOVacio_debeIndicarQueEsObligatorio(
            String idEjemplar
    ) {

        SolicitarPrestamoRequest request =
                new SolicitarPrestamoRequest(UUID_USUARIO, idEjemplar);


        assertTrue(tieneViolacion(
                request,
                "idEjemplar",
                "El id del ejemplar es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdEjemplarNoEsUuid_debeIndicarFormatoInvalido() {

        SolicitarPrestamoRequest request =
                new SolicitarPrestamoRequest(UUID_USUARIO, "ejemplar-invalido");


        assertTrue(tieneViolacion(
                request,
                "idEjemplar",
                "El id del ejemplar debe ser un UUID valido"
        ));
    }

    private boolean tieneViolacion(
            SolicitarPrestamoRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}