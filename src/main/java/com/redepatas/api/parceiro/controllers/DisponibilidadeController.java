package com.redepatas.api.parceiro.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.AtualizarDisponibilidadeDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.CriarDisponibilidadeDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.DisponibilidadeResponseDTO;
import com.redepatas.api.parceiro.dtos.DisponibilidadeDtos.SlotsDisponiveisDTO;
import com.redepatas.api.parceiro.services.DisponibilidadeServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/servicos/{servicoId}/disponibilidade")
@Validated
public class DisponibilidadeController {

    @Autowired
    private DisponibilidadeServicoService disponibilidadeService;

    @PostMapping("/parceiro/{parceiroId}")
    public ResponseEntity<?> criarDisponibilidade(
            @PathVariable UUID servicoId,
            @PathVariable UUID parceiroId,
            @Valid @RequestBody CriarDisponibilidadeDTO dto) {
        try {
            DisponibilidadeResponseDTO disponibilidade = disponibilidadeService.criarDisponibilidade(servicoId, parceiroId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>> listarDisponibilidades(@PathVariable UUID servicoId) {
        List<DisponibilidadeResponseDTO> disponibilidades = disponibilidadeService.listarDisponibilidadePorServico(servicoId);
        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/slots")
    public ResponseEntity<?> obterSlotsDisponiveis(
            @PathVariable UUID servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        try {
            List<SlotsDisponiveisDTO> slots = disponibilidadeService.obterSlotsDisponiveis(servicoId, data);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter slots disponíveis: " + e.getMessage());
        }
    }

    @PatchMapping("/{disponibilidadeId}")
    public ResponseEntity<?> atualizarDisponibilidade(
            @PathVariable UUID servicoId,
            @PathVariable UUID disponibilidadeId,
            @Valid @RequestBody AtualizarDisponibilidadeDTO dto) {
        try {
            DisponibilidadeResponseDTO disponibilidade = disponibilidadeService.atualizarDisponibilidade(disponibilidadeId, dto);
            return ResponseEntity.ok(disponibilidade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @DeleteMapping("/{disponibilidadeId}")
    public ResponseEntity<?> deletarDisponibilidade(
            @PathVariable UUID servicoId,
            @PathVariable UUID disponibilidadeId) {
        try {
            disponibilidadeService.deletarDisponibilidade(disponibilidadeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
}
