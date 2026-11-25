package com.biblioteca.auth.aplication;

import com.biblioteca.auth.domain.model.gateway.EncrypterGateway;
import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import com.biblioteca.auth.domain.model.gateway.PasswordResetTokenGateway;
import com.biblioteca.auth.domain.model.gateway.UsuarioGateway;
import com.biblioteca.auth.domain.usecase.UsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Usuarioconfig {
    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateway usuarioGateway,
                                         EncrypterGateway encrypterGateway,
                                         PasswordResetTokenGateway passwordResetTokenGateway,
                                         NotificationGateway notificationGateway){
        return new UsuarioUseCase(usuarioGateway, encrypterGateway, passwordResetTokenGateway, notificationGateway);
    }
}
