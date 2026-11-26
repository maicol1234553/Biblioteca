package com.biblioteca.auth.infraestructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncrypterGatewayImplTest {

    private final EncrypterGatewayImpl encrypter = new EncrypterGatewayImpl();

    @Test
    void encrypt_debeRetornarUnaPasswordEncriptada() {
        // Arrange
        String password = "ClaveSegura123";

        // Act
        String passwordEncriptada = encrypter.encrypt(password);

        // Assert
        assertNotNull(passwordEncriptada);
        assertFalse(password.equals(passwordEncriptada));
        assertTrue(encrypter.checkPass(password, passwordEncriptada));
    }

    @Test
    void checkPass_cuandoLaPasswordNoCoincide_debeRetornarFalse() {
        // Arrange
        String passwordCorrecta = "ClaveSegura123";
        String passwordIncorrecta = "OtraClave456";
        String passwordEncriptada = encrypter.encrypt(passwordCorrecta);

        // Act
        Boolean resultado = encrypter.checkPass(passwordIncorrecta, passwordEncriptada);

        // Assert
        assertFalse(resultado);
    }
}