package com.biblioteca.notificacion.infraestructure.entry_points.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantillaRequestTest {

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
    void validar_cuandoDatosSonValidos_noDebeTenerViolaciones() {

        PlantillaRequest request = crearRequest(
                "Aviso de préstamo",
                "Préstamo confirmado",
                "Tu préstamo fue confirmado.",
                "PRESTAMO",
                "ACTIVA"
        );


        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validar_cuandoNombreEstaVacio_debeIndicarQueEsObligatorio() {
        PlantillaRequest request = crearRequest(
                " ",
                "Asunto",
                "Contenido",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "nombre",
                "El nombre es obligatorio"
        ));
    }

    @Test
    void validar_cuandoNombreSupera150Caracteres_debeIndicarLimite() {
        PlantillaRequest request = crearRequest(
                "a".repeat(151),
                "Asunto",
                "Contenido",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "nombre",
                "El nombre no puede superar 150 caracteres"
        ));
    }

    @Test
    void validar_cuandoAsuntoEstaVacio_debeIndicarQueEsObligatorio() {
        PlantillaRequest request = crearRequest(
                "Nombre",
                "",
                "Contenido",
                null,
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
        PlantillaRequest request = crearRequest(
                "Nombre",
                "a".repeat(256),
                "Contenido",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "asunto",
                "El asunto no puede superar 255 caracteres"
        ));
    }

    @Test
    void validar_cuandoContenidoEstaVacio_debeIndicarQueEsObligatorio() {
        PlantillaRequest request = crearRequest(
                "Nombre",
                "Asunto",
                " ",
                null,
                null
        );

        assertTrue(tieneViolacion(
                request,
                "contenido",
                "El contenido es obligatorio"
        ));
    }

    @Test
    void validar_cuandoTipoYEstadoSonNull_noDebeTenerViolaciones() {

        PlantillaRequest request = crearRequest(
                "Nombre",
                "Asunto",
                "Contenido",
                null,
                null
        );


        assertTrue(validator.validate(request).isEmpty());
    }

    private PlantillaRequest crearRequest(
            String nombre,
            String asunto,
            String contenido,
            String tipo,
            String estado
    ) {
        return new PlantillaRequest(nombre, asunto, contenido, tipo, estado);
    }

    private boolean tieneViolacion(
            PlantillaRequest request,
            String campo,
            String mensaje
    ) {
        return validator.validate(request).stream().anyMatch(violacion ->
                violacion.getPropertyPath().toString().equals(campo)
                        && violacion.getMessage().equals(mensaje)
        );
    }
}