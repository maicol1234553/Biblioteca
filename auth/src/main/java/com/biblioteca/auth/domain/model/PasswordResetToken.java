package com.biblioteca.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordResetToken {

    private String idToken;
    private String idUsuario;
    private String tokenHash;
    private LocalDateTime fechaExpiracion;
    private Boolean usado;
}
