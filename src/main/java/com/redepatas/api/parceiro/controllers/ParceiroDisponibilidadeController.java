package com.redepatas.api.parceiro.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.DisponibilidadeResponseDTO;
import com.redepatas.api.parceiro.services.DisponibilidadeServicoService;

@RestController
@RequestMapping("/parceiros/{parceiroId}/disponibilidades")
public class ParceiroDisponibilidadeController {

    @Autowired
    private DisponibilidadeServicoService disponibilidadeService;

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>> listarDisponibilidadesPorParceiro(@PathVariable UUID parceiroId) {
        List<DisponibilidadeResponseDTO> disponibilidades = disponibilidadeService.listarDisponibilidadePorParceiro(parceiroId);
        return ResponseEntity.ok(disponibilidades);
    }
}
