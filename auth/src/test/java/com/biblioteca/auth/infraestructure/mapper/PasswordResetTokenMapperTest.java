package com.biblioteca.auth.infraestructure.mapper;

import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.infraestructure.driver_adapters.PasswordResetTokenData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordResetTokenMapperTest {

    private final PasswordResetTokenMapper mapper = new PasswordResetTokenMapper();

    @Test
    void toToken_cuandoRecibeDatos_debeMapearTodosLosCampos() {

        LocalDateTime fechaExpiracion = LocalDateTime.of(2026, 11, 25, 15, 30);

        PasswordResetTokenData tokenData = new PasswordResetTokenData(
                "token-1",
                "usuario-1",
                "hash-abc123",
                fechaExpiracion,
                false
        );


        PasswordResetToken resultado = mapper.toToken(tokenData);


        assertNotNull(resultado);
        assertAll(
                () -> assertEquals("token-1", resultado.getIdToken()),
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("hash-abc123", resultado.getTokenHash()),
                () -> assertEquals(fechaExpiracion, resultado.getFechaExpiracion()),
                () -> assertEquals(false, resultado.getUsado())
        );
    }

    @Test
    void toToken_cuandoRecibeNull_debeRetornarNull() {

        PasswordResetTokenData tokenData = null;


        PasswordResetToken resultado = mapper.toToken(tokenData);


        assertNull(resultado);
    }

    @Test
    void toTokenData_cuandoRecibeToken_debeMapearTodosLosCampos() {

        LocalDateTime fechaExpiracion = LocalDateTime.of(2026, 11, 25, 15, 30);

        PasswordResetToken token = new PasswordResetToken(
                "token-2",
                "usuario-2",
                "hash-xyz789",
                fechaExpiracion,
                true
        );


        PasswordResetTokenData resultado = mapper.toTokenData(token);


        assertNotNull(resultado);
        assertAll(
                () -> assertEquals("token-2", resultado.getIdToken()),
                () -> assertEquals("usuario-2", resultado.getIdUsuario()),
                () -> assertEquals("hash-xyz789", resultado.getTokenHash()),
                () -> assertEquals(fechaExpiracion, resultado.getFechaExpiracion()),
                () -> assertEquals(true, resultado.getUsado())
        );
    }
}