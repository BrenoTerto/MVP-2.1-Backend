package com.redepatas.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.ConfirmationToken;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM ConfirmationToken c WHERE c.user = :user")
    void deleteByUser(@Param("user") ClientModel user);
}
