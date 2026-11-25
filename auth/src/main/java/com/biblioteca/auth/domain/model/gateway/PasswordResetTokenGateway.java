package com.biblioteca.auth.domain.model.gateway;

import com.biblioteca.auth.domain.model.PasswordResetToken;

public interface PasswordResetTokenGateway {

    PasswordResetToken guardarToken(PasswordResetToken token);

    PasswordResetToken buscarPorTokenHash(String tokenHash);

    PasswordResetToken actualizarToken(PasswordResetToken token);
}
