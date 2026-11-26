package com.biblioteca.catalogo.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   ", "\t"})
    void desde_cuandoValorEsNullOVacio_debeRetornarUsuario(String valor) {
        // Act
        Rol resultado = Rol.desde(valor);

        // Assert
        assertEquals(Rol.USUARIO, resultado);
    }

    @ParameterizedTest
    @CsvSource({
            "ADMIN, ADMINISTRADOR",
            "admin, ADMINISTRADOR",
            "ADMINISTRADOR, ADMINISTRADOR",
            "administrador, ADMINISTRADOR",
            "USER, USUARIO",
            "user, USUARIO",
            "USUARIO, USUARIO",
            "usuario, USUARIO"
    })
    void desde_cuandoValorEsValido_debeRetornarElRolCorrespondiente(
            String valor,
            Rol esperado
    ) {
        // Act
        Rol resultado = Rol.desde(valor);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    void desde_cuandoValorTieneEspaciosAlrededor_debeNormalizarlo() {
        // Act
        Rol resultado = Rol.desde("  admin  ");

        // Assert
        assertEquals(Rol.ADMINISTRADOR, resultado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVITADO", "ROOT", "ADMINISTRADORX", "123"})
    void desde_cuandoValorNoEsValido_debeLanzarExcepcion(String valor) {
        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> Rol.desde(valor)
        );

        // Assert
        assertEquals(
                "El rol debe ser ADMINISTRADOR o USUARIO",
                excepcion.getMessage()
        );
    }
}