package com.redepatas.api.parceiro.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redepatas.api.parceiro.models.PartnerConfirmationToken;

public interface PartnerConfirmationTokenRepository extends JpaRepository<PartnerConfirmationToken, UUID> {
    Optional<PartnerConfirmationToken> findByToken(String token);
}
