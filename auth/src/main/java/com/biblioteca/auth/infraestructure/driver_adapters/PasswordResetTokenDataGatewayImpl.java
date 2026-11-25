package com.biblioteca.auth.infraestructure.driver_adapters;

import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.domain.model.gateway.PasswordResetTokenGateway;
import com.biblioteca.auth.infraestructure.mapper.PasswordResetTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PasswordResetTokenDataGatewayImpl implements PasswordResetTokenGateway {

    private final PasswordResetTokenDataJpaRepository passwordResetTokenDataJpaRepository;
    private final PasswordResetTokenMapper passwordResetTokenMapper;

    @Override
    public PasswordResetToken guardarToken(PasswordResetToken token) {
        return passwordResetTokenMapper.toToken(
                passwordResetTokenDataJpaRepository.save(passwordResetTokenMapper.toTokenData(token)));
    }

    @Override
    public PasswordResetToken buscarPorTokenHash(String tokenHash) {
        return passwordResetTokenDataJpaRepository.findByTokenHash(tokenHash)
                .map(passwordResetTokenMapper::toToken)
                .orElse(null);
    }

    @Override
    public PasswordResetToken actualizarToken(PasswordResetToken token) {
        return guardarToken(token);
    }
}
