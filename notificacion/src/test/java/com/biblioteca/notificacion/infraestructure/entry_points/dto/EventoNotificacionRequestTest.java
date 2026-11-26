package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoNotificacionRequestTest {

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
    void validar_cuandoLosDatosSonCorrectos_noDebeTenerViolaciones() {
        // Arrange
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                "PRESTAMO_VENCIDO",
                UUID_VALIDO,
                "prestamo-123",
                "El préstamo está vencido",
                LocalDateTime.of(2026, 11, 26, 10, 0)
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoTipoEstaVacio_debeIndicarQueEsObligatorio() {
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                " ",
                UUID_VALIDO,
                null,
                "Mensaje de prueba",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "tipo",
                "El tipo de notificacion es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                "PRESTAMO_VENCIDO",
                "",
                null,
                "Mensaje de prueba",
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
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                "PRESTAMO_VENCIDO",
                "usuario-invalido",
                null,
                "Mensaje de prueba",
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idUsuario",
                "El id del usuario debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoMensajeEstaVacio_debeIndicarQueEsObligatorio() {
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                "PRESTAMO_VENCIDO",
                UUID_VALIDO,
                null,
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
    void validar_cuandoReferenciaYFechaSonNull_noDebeTenerViolaciones() {
        // Arrange
        EventoNotificacionRequest request = new EventoNotificacionRequest(
                "PRESTAMO_VENCIDO",
                UUID_VALIDO,
                null,
                "Mensaje de prueba",
                null
        );

        // Act y Assert
        assertTrue(validator.validate(request).isEmpty());
    }

    private boolean tieneViolacion(
            EventoNotificacionRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}