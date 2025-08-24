package com.redepatas.api.parceiro.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.cliente.models.PasswordResetToken;
import com.redepatas.api.cliente.repositories.PasswordResetTokenRepository;
import com.redepatas.api.cliente.services.EmailService;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.repositories.PartnerRepository;

import jakarta.mail.MessagingException;

@Service
public class PartnerPasswordResetService {

    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;


    public void requestReset(String email) {
        PartnerModel partner = partnerRepository.findByEmailContato(email);

        if (partner != null) {
            String rawToken = tokenService.generateTokenPassword(email);
            try {

                emailService.enviarRecovery(email, rawToken);
            } catch (MessagingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao enviar email");
            }
        }
    }

    public void confirmReset(String email, String rawToken, String newPassword) {
        PasswordResetToken token = tokenService.validarTokenDeReset(rawToken);
        if (token == null || token.getExpiration().isBefore(LocalDateTime.now())
                || !token.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido ou expirado");
        }
        PartnerModel partner = partnerRepository.findByEmailContato(email);
        if (partner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parceiro não encontrado");
        }
        partner.setPassword(passwordEncoder.encode(newPassword));
        partnerRepository.save(partner);

        passwordResetTokenRepository.delete(token);
    }

}
