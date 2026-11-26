package com.biblioteca.auth.infraestructure.driver_adapters;

import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.infraestructure.mapper.PasswordResetTokenMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenDataGatewayImplTest {

    @Mock
    private PasswordResetTokenDataJpaRepository passwordResetTokenDataJpaRepository;

    @Mock
    private PasswordResetTokenMapper passwordResetTokenMapper;

    @InjectMocks
    private PasswordResetTokenDataGatewayImpl passwordResetTokenDataGateway;

    @Test
    void guardarToken_debeConvertirGuardarYRetornarToken() {

        PasswordResetToken token = org.mockito.Mockito.mock(PasswordResetToken.class);
        PasswordResetTokenData tokenData =
                org.mockito.Mockito.mock(PasswordResetTokenData.class);
        PasswordResetTokenData tokenDataGuardado =
                org.mockito.Mockito.mock(PasswordResetTokenData.class);
        PasswordResetToken tokenConvertido =
                org.mockito.Mockito.mock(PasswordResetToken.class);

        when(passwordResetTokenMapper.toTokenData(token)).thenReturn(tokenData);
        when(passwordResetTokenDataJpaRepository.save(tokenData))
                .thenReturn(tokenDataGuardado);
        when(passwordResetTokenMapper.toToken(tokenDataGuardado))
                .thenReturn(tokenConvertido);


        PasswordResetToken resultado =
                passwordResetTokenDataGateway.guardarToken(token);


        assertSame(tokenConvertido, resultado);
        verify(passwordResetTokenMapper).toTokenData(token);
        verify(passwordResetTokenDataJpaRepository).save(tokenData);
        verify(passwordResetTokenMapper).toToken(tokenDataGuardado);
    }

    @Test
    void buscarPorTokenHash_cuandoExiste_debeRetornarTokenConvertido() {

        String tokenHash = "hash-del-token";
        PasswordResetTokenData tokenData =
                org.mockito.Mockito.mock(PasswordResetTokenData.class);
        PasswordResetToken token =
                org.mockito.Mockito.mock(PasswordResetToken.class);

        when(passwordResetTokenDataJpaRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(tokenData));
        when(passwordResetTokenMapper.toToken(tokenData)).thenReturn(token);


        PasswordResetToken resultado =
                passwordResetTokenDataGateway.buscarPorTokenHash(tokenHash);


        assertSame(token, resultado);
        verify(passwordResetTokenDataJpaRepository).findByTokenHash(tokenHash);
        verify(passwordResetTokenMapper).toToken(tokenData);
    }

    @Test
    void buscarPorTokenHash_cuandoNoExiste_debeRetornarNull() {

        String tokenHash = "hash-inexistente";

        when(passwordResetTokenDataJpaRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());


        PasswordResetToken resultado =
                passwordResetTokenDataGateway.buscarPorTokenHash(tokenHash);


        assertNull(resultado);
        verify(passwordResetTokenDataJpaRepository).findByTokenHash(tokenHash);
        verifyNoInteractions(passwordResetTokenMapper);
    }

    @Test
    void actualizarToken_debeGuardarYRetornarTokenActualizado() {

        PasswordResetToken token = org.mockito.Mockito.mock(PasswordResetToken.class);
        PasswordResetTokenData tokenData =
                org.mockito.Mockito.mock(PasswordResetTokenData.class);
        PasswordResetTokenData tokenDataGuardado =
                org.mockito.Mockito.mock(PasswordResetTokenData.class);
        PasswordResetToken tokenActualizado =
                org.mockito.Mockito.mock(PasswordResetToken.class);

        when(passwordResetTokenMapper.toTokenData(token)).thenReturn(tokenData);
        when(passwordResetTokenDataJpaRepository.save(tokenData))
                .thenReturn(tokenDataGuardado);
        when(passwordResetTokenMapper.toToken(tokenDataGuardado))
                .thenReturn(tokenActualizado);


        PasswordResetToken resultado =
                passwordResetTokenDataGateway.actualizarToken(token);


        assertSame(tokenActualizado, resultado);
        verify(passwordResetTokenMapper).toTokenData(token);
        verify(passwordResetTokenDataJpaRepository).save(tokenData);
        verify(passwordResetTokenMapper).toToken(tokenDataGuardado);
    }
}