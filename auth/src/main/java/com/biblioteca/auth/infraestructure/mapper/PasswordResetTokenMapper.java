package com.biblioteca.auth.infraestructure.mapper;

import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.infraestructure.driver_adapters.PasswordResetTokenData;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenMapper {

    public PasswordResetToken toToken(PasswordResetTokenData tokenData) {
        if (tokenData == null) {
            return null;
        }

        return new PasswordResetToken(
                tokenData.getIdToken(),
                tokenData.getIdUsuario(),
                tokenData.getTokenHash(),
                tokenData.getFechaExpiracion(),
                tokenData.getUsado()
        );
    }

    public PasswordResetTokenData toTokenData(PasswordResetToken token) {
        return new PasswordResetTokenData(
                token.getIdToken(),
                token.getIdUsuario(),
                token.getTokenHash(),
                token.getFechaExpiracion(),
                token.getUsado()
        );
    }
}
