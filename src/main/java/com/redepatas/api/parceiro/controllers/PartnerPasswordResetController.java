package com.redepatas.api.parceiro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import com.redepatas.api.parceiro.dtos.PartnerPasswordResetDtos.PartnerResetRequestDto;
import com.redepatas.api.parceiro.dtos.PartnerPasswordResetDtos.PartnerResetConfirmDto;
import com.redepatas.api.parceiro.services.PartnerPasswordResetService;
import com.redepatas.api.parceiro.services.PasswordResetRateLimiter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/partners/password")
@Validated
public class PartnerPasswordResetController {

    @Autowired
    private PartnerPasswordResetService passwordResetService;
    @Autowired
    private PasswordResetRateLimiter rateLimiter;

    @PostMapping("/reset-request")
    public ResponseEntity<?> request(@RequestBody @Valid PartnerResetRequestDto dto, HttpServletRequest request) {
        String ip = extractClientIp(request);
        boolean allowed = rateLimiter.allow(dto.email(), ip);
        if (allowed) {
            passwordResetService.requestReset(dto.email());
        }
        return ResponseEntity.ok().body("Se o e-mail existir, enviaremos instruções de redefinição.");
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<?> confirm(@RequestBody @Valid PartnerResetConfirmDto dto) {
        passwordResetService.confirmReset(dto.token(), dto.newPassword());
        return ResponseEntity.ok().body("Senha alterada com sucesso");
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            // Pode vir lista separada por vírgula, pegar primeiro
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
