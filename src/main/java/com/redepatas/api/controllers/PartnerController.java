package com.redepatas.api.controllers;

import com.redepatas.api.dtos.PartnerDtos.PartenerServicesDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.dtos.PartnerDtos.getPartnerDtos;
import com.redepatas.api.services.PartnerService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @PostMapping("/create")
    public ResponseEntity<String> createPartner(@RequestBody @Valid PartnerRecordDto partnerDto) {
        String savedPartner = partnerService.createPartner(partnerDto);
        return ResponseEntity.status(201).body(savedPartner);
    }

    @PostMapping("/getAll")
    public List<PartnerDto> getAllPartner(
            @RequestBody @Valid getPartnerDtos dtos,
            @AuthenticationPrincipal UserDetails userDetails) {
        return partnerService.getAllPartners(dtos.porte(), userDetails.getUsername(), dtos.nomeServico(), dtos.rua(), dtos.bairro(), dtos.cidade());
    }

    @GetMapping("getServices/{idPartner}/{nomeServico}")
    public ResponseEntity<PartenerServicesDto> getPartnerService(
            @PathVariable UUID idPartner,
            @PathVariable String nomeServico) {
        return ResponseEntity.ok(partnerService.getOnlyPartner(idPartner, nomeServico));
    }
}
