package com.redepatas.api.controllers;

import com.redepatas.api.dtos.PartnerDtos.GetPartnerServiceDto;
import com.redepatas.api.dtos.PartnerDtos.PartenerServicesDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerDto;
import com.redepatas.api.dtos.PartnerDtos.PartnerRecordDto;
import com.redepatas.api.dtos.PartnerDtos.getPartnerDtos;
import com.redepatas.api.services.PartnerService;

import jakarta.validation.Valid;

import java.time.LocalDate;
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
        return partnerService.getAllPartners(dtos.porte(), userDetails.getUsername(), dtos.nomeServico(), dtos.rua(),
                dtos.bairro(), dtos.cidade());
    }

    @PostMapping("/getServices")
    public ResponseEntity<PartenerServicesDto> getPartnerService(
            @RequestBody @Valid GetPartnerServiceDto dto) {
        LocalDate data = dto.data() != null ? LocalDate.parse(dto.data()) : null;
        return ResponseEntity.ok(partnerService.getOnlyPartner(dto.idParceiro(), dto.nomeServico(), data));
    }
}
