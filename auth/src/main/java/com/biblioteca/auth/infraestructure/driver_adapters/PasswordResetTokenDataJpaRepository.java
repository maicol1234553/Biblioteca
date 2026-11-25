package com.biblioteca.auth.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenDataJpaRepository extends JpaRepository<PasswordResetTokenData, String> {

    Optional<PasswordResetTokenData> findByTokenHash(String tokenHash);
}
