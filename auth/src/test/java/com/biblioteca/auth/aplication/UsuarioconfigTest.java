package com.biblioteca.auth.aplication;

import com.biblioteca.auth.domain.model.gateway.EncrypterGateway;
import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import com.biblioteca.auth.domain.model.gateway.PasswordResetTokenGateway;
import com.biblioteca.auth.domain.model.gateway.UsuarioGateway;
import com.biblioteca.auth.domain.usecase.UsuarioUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UsuarioconfigTest {

    @Test
    void usuarioUseCase_debeCrearUnaInstanciaConLasDependencias() {
        // Arrange
        Usuarioconfig config = new Usuarioconfig();

        UsuarioGateway usuarioGateway = mock(UsuarioGateway.class);
        EncrypterGateway encrypterGateway = mock(EncrypterGateway.class);
        PasswordResetTokenGateway passwordResetTokenGateway =
                mock(PasswordResetTokenGateway.class);
        NotificationGateway notificationGateway = mock(NotificationGateway.class);

        // Act
        UsuarioUseCase resultado = config.usuarioUseCase(
                usuarioGateway,
                encrypterGateway,
                passwordResetTokenGateway,
                notificationGateway
        );

        // Assert
        assertNotNull(resultado);
    }
}