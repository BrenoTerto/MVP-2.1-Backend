package com.redepatas.api.parceiro.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.ServicoDtos.AtualizarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarServicoDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.ServicoResponseDTO;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.services.ServicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/parceiros/servicos")
@Validated
public class ServicosController {

    @Autowired
    private ServicoService servicoService;

    @PostMapping("/criar")
    public ResponseEntity<?> criarServico(@Valid @RequestBody CriarServicoDTO dto) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }

            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();

            // Definir o parceiroId do DTO como o ID do parceiro logado
            dto.setParceiroId(parceiroLogado.getIdPartner());

            servicoService.criarServico(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Serviço criado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/getServico")
    public ResponseEntity<?> listarMeusServicos() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }

            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();

            List<ServicoResponseDTO> servicos = servicoService.listarServicosPorParceiro(parceiroLogado.getIdPartner());
            return ResponseEntity.ok(servicos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarServicoPorId(@PathVariable UUID id) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }

            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();

            ServicoResponseDTO servico = servicoService.buscarServicoPorId(id, parceiroLogado.getIdPartner());
            return ResponseEntity.ok(servico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarServicoParcial(@PathVariable UUID id,
            @Valid @RequestBody AtualizarServicoDTO dto) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }

            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();

            servicoService.atualizarServicoParcialPorParceiro(id, dto,
                    parceiroLogado.getIdPartner());
            return ResponseEntity.ok("Serviço atualizado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarServico(@PathVariable UUID id) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }

            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();

            servicoService.deletarServicoPorParceiro(id, parceiroLogado.getIdPartner());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/tipos-permitidos")
    public ResponseEntity<List<String>> listarTiposPermitidos() {
        List<String> tipos = servicoService.listarTiposPermitidos();
        return ResponseEntity.ok(tipos);
    }

}
