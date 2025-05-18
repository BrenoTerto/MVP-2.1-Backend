package com.redepatas.api.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenHash;
    private String email;
    private LocalDateTime expiration;

    public PasswordResetToken(String tokenHash, String email, LocalDateTime expiration) {
        this.tokenHash = tokenHash;
        this.email = email;
        this.expiration = expiration;
    }

}
