package com.redepatas.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PasswordResetToken;
import com.redepatas.api.cliente.repositories.PasswordResetTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    @Autowired
    PasswordResetTokenRepository tokenRepository;

    public String generateToken(TokenUser user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getLogin())
                    .withClaim("role", user.getRole().toString())
                    .withClaim("id", user.getId().toString())
                    .withClaim("nome", user.getNome())
                    .withExpiresAt(genereteExperiatiopnDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro enquanto o token estava sendo gerado", exception);
        }
    }

    public String generateToken(ClientModel user) {
        return generateToken((TokenUser) user);
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String generateTokenPassword(String email) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            String hashedToken = Base64.getEncoder().encodeToString(hashBytes);

            LocalDateTime expiration = LocalDateTime.now().plusHours(2);

            PasswordResetToken tokenEntity = new PasswordResetToken(hashedToken, email, expiration);
            tokenRepository.save(tokenEntity);

            return rawToken;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar e salvar token", e);
        }
    }

    public PasswordResetToken validarTokenDeReset(String tokenPuro) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(tokenPuro.getBytes(StandardCharsets.UTF_8));
            String hashCalculado = Base64.getEncoder().encodeToString(hashBytes);
            PasswordResetToken token = tokenRepository.findByTokenHash(hashCalculado);
            if (token == null || token.getExpiration().isBefore(LocalDateTime.now())) {
                return null;
            }

            return token;

        } catch (Exception e) {
            return null;
        }
    }

    private Instant genereteExperiatiopnDate() {
        return LocalDateTime.now().plusHours(25).toInstant(ZoneOffset.of("-03:00"));
    }
}
