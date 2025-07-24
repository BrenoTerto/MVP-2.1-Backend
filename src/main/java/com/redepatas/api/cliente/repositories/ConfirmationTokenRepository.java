package com.redepatas.api.cliente.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redepatas.api.cliente.models.ConfirmationToken;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);
}
