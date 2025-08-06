package com.redepatas.api.parceiro.controllers;

import com.redepatas.api.cliente.dtos.ReponseLoginDto;
import com.redepatas.api.cliente.dtos.petDtos.NewPetDto;
import com.redepatas.api.cliente.models.AuthenticationDTO;
import com.redepatas.api.infra.security.TokenService;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerDto;
import com.redepatas.api.parceiro.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.services.PartnerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

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

            // if (!partner.isEmailConfirmado()) {
            // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cadastro não
            // confirmado!");
            // }

            var token = tokenService.generateToken(partner);
            return ResponseEntity.ok(new ReponseLoginDto(token));

        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.");
        }
    }

    // @PostMapping("/getServices")
    // public ResponseEntity<PartenerServicesDto> getPartnerService(
    // @RequestBody @Valid GetPartnerServiceDto dto) {
    // LocalDate data = dto.data() != null ? LocalDate.parse(dto.data()) : null;
    // return ResponseEntity.ok(partnerService.getOnlyPartner(dto.idParceiro(),
    // dto.nomeServico(), data));
    // }
}
