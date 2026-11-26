package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LibroDisponibleRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private static final String UUID_USUARIO =
            "123e4567-e89b-12d3-a456-426614174000";
    private static final String UUID_LIBRO =
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

        LibroDisponibleRequest request = crearRequest(
                UUID_USUARIO,
                UUID_LIBRO,
                "persona@correo.com",
                "El libro ya está disponible"
        );


        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoIdUsuarioEstaVacio_debeIndicarQueEsObligatorio() {
        LibroDisponibleRequest request = crearRequest(
                " ",
                UUID_LIBRO,
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
        LibroDisponibleRequest request = crearRequest(
                "usuario-invalido",
                UUID_LIBRO,
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
    void validar_cuandoIdLibroEstaVacio_debeIndicarQueEsObligatorio() {
        LibroDisponibleRequest request = crearRequest(
                UUID_USUARIO,
                "",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idLibro",
                "El id del libro es obligatorio"
        ));
    }

    @Test
    void validar_cuandoIdLibroNoEsUuid_debeIndicarFormatoInvalido() {
        LibroDisponibleRequest request = crearRequest(
                UUID_USUARIO,
                "libro-invalido",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "idLibro",
                "El id del libro debe ser un UUID valido"
        ));
    }

    @Test
    void validar_cuandoDestinatarioNoEsCorreo_debeIndicarFormatoInvalido() {
        LibroDisponibleRequest request = crearRequest(
                UUID_USUARIO,
                UUID_LIBRO,
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

        LibroDisponibleRequest request = crearRequest(
                UUID_USUARIO,
                UUID_LIBRO,
                null,
                null
        );


        assertTrue(validator.validate(request).isEmpty());
    }

    private LibroDisponibleRequest crearRequest(
            String idUsuario,
            String idLibro,
            String destinatario,
            String mensaje
    ) {
        return new LibroDisponibleRequest(
                idUsuario,
                idLibro,
                destinatario,
                mensaje
        );
    }

    private boolean tieneViolacion(
            LibroDisponibleRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}