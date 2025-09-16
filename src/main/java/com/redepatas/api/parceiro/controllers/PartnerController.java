package com.redepatas.api.parceiro.controllers;

import com.redepatas.api.cliente.dtos.ReponseLoginDto;
import com.redepatas.api.cliente.models.AuthenticationDTO;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerProfileDto;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.PartnerConfirmationToken;
import com.redepatas.api.parceiro.repositories.PartnerConfirmationTokenRepository;
import com.redepatas.api.parceiro.repositories.PartnerRepository;
import com.redepatas.api.parceiro.services.PartnerService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.AuthenticationException;

import com.redepatas.api.parceiro.dtos.PartnerDtos.AtualizarParceiroBasicDTO;
import com.redepatas.api.parceiro.dtos.PartnerDtos.AtualizarEnderecoPartnerDTO;
import com.redepatas.api.cliente.dtos.ChangePasswordDto;
import com.redepatas.api.parceiro.services.PasswordResetRateLimiter;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PartnerConfirmationTokenRepository partnerConfirmationTokenRepository;

    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private PasswordResetRateLimiter rateLimiter;

    @PostMapping("/create")
    public ResponseEntity<String> createPartner(
            @RequestPart(value = "partnerData", required = false) @Validated PartnerRecordDto partnerDataJson,
            @RequestPart(value = "image", required = false) MultipartFile image)
            throws com.fasterxml.jackson.core.JsonProcessingException {

        // Validar se a imagem foi fornecida
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("A imagem é obrigatória para o cadastro do parceiro.");
        }

        // Chamar o service - exceções são tratadas pelo PartnerExceptionHandler
        String savedPartner = partnerService.createPartner(partnerDataJson, image);
        return ResponseEntity.status(201).body(savedPartner);
    }

    @PostMapping("/login")
    public ResponseEntity<ReponseLoginDto> login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var clientPassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(clientPassword);

            PartnerModel partner = (PartnerModel) auth.getPrincipal();

            if (!partner.isEmailConfirmado()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cadastro não confirmado!");
            }

            var token = tokenService.generateToken(partner);
            return ResponseEntity.ok(new ReponseLoginDto(token));

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam("token") String token) {
        var optional = partnerConfirmationTokenRepository.findByToken(token);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body("Token inválido ou não encontrado.");
        }
        PartnerConfirmationToken confirmationToken = optional.get();
        if (confirmationToken.getConfirmedAt() != null) {
            return ResponseEntity.badRequest().body("Conta já confirmada.");
        }
        if (confirmationToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expirado.");
        }
        PartnerModel partner = confirmationToken.getPartner();
        partner.setEmailConfirmado(true);
        confirmationToken.setConfirmedAt(java.time.LocalDateTime.now());
        partnerConfirmationTokenRepository.save(confirmationToken);
        partnerRepository.save(partner);
        return ResponseEntity.ok("Cadastro de parceiro confirmado com sucesso!");
    }

    @PostMapping("/resendConfirmation/{email}")
    public ResponseEntity<Void> resendConfirmation(@PathVariable("email") String email, HttpServletRequest request) {
        String ip = extractClientIp(request);
        boolean allowed = rateLimiter.allow(email, ip);
        if (allowed) {
            partnerService.resendConfirmationEmail(email);
        }
        return ResponseEntity.noContent().build();
    }

    // @PostMapping("/getServices")
    // public ResponseEntity<PartenerServicesDto> getPartnerService(
    // @RequestBody @Valid GetPartnerServiceDto dto) {
    // LocalDate data = dto.data() != null ? LocalDate.parse(dto.data()) : null;
    // return ResponseEntity.ok(partnerService.getOnlyPartner(dto.idParceiro(),
    // dto.nomeServico(), data));
    // }

    @PutMapping("/me/basic")
    public ResponseEntity<String> updateBasic(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AtualizarParceiroBasicDTO dto) {
        String login = userDetails.getUsername();
        String resp = partnerService.updateBasic(login, dto.name(), dto.descricao());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<PartnerProfileDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        PartnerProfileDto profile = partnerService.getProfile(login);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/address")
    public ResponseEntity<String> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AtualizarEnderecoPartnerDTO dto) {
        String login = userDetails.getUsername();
        String resp = partnerService.updateAddress(login, dto.rua(), dto.bairro(), dto.cidade(), dto.estado(),
                dto.cep(), dto.numero(), dto.complemento(), dto.lugar());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordDto dto) {
        String login = userDetails.getUsername();
        String resp = partnerService.changePassword(login, dto.oldPassword(), dto.newPassword(), authenticationManager);
        return ResponseEntity.ok(resp);
    }
    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
