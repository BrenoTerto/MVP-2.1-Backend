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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.ServicoDtos.AdicionalResponseDTO;
import com.redepatas.api.parceiro.dtos.ServicoDtos.CriarAdicionalDTO;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.services.AdicionaisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/parceiros/servicos/{servicoId}/adicionais")
@Validated
public class AdicionaisController {
    
    @Autowired
    private AdicionaisService adicionaisService;
    
    @PostMapping
    public ResponseEntity<?> adicionarAdicional(
            @PathVariable UUID servicoId, 
            @Valid @RequestBody CriarAdicionalDTO dto) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado: usuário não autenticado como parceiro");
            }
            
            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();
            
            AdicionalResponseDTO adicional = adicionaisService.adicionarAdicional(servicoId, dto, parceiroLogado.getIdPartner());
            return ResponseEntity.status(HttpStatus.CREATED).body(adicional);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> listarAdicionaisPorServico(@PathVariable UUID servicoId) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado: usuário não autenticado como parceiro");
            }
            
            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();
            
            List<AdicionalResponseDTO> adicionais = adicionaisService.listarAdicionaisPorServico(servicoId, parceiroLogado.getIdPartner());
            return ResponseEntity.ok(adicionais);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarAdicionalPorId(@PathVariable UUID servicoId, @PathVariable UUID id) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado: usuário não autenticado como parceiro");
            }
            
            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();
            
            Optional<AdicionalResponseDTO> adicional = adicionaisService.buscarAdicionalPorId(id, servicoId, parceiroLogado.getIdPartner());
            if (adicional.isPresent()) {
                return ResponseEntity.ok(adicional.get());
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAdicional(
            @PathVariable UUID servicoId, 
            @PathVariable UUID id, 
            @Valid @RequestBody CriarAdicionalDTO dto) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado: usuário não autenticado como parceiro");
            }
            
            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();
            
            AdicionalResponseDTO adicional = adicionaisService.atualizarAdicional(id, dto, servicoId, parceiroLogado.getIdPartner());
            return ResponseEntity.ok(adicional);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarAdicional(@PathVariable UUID servicoId, @PathVariable UUID id) {
        try {
            // Obter o parceiro autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acesso negado: usuário não autenticado como parceiro");
            }
            
            PartnerModel parceiroLogado = (PartnerModel) authentication.getPrincipal();
            
            adicionaisService.deletarAdicional(id, servicoId, parceiroLogado.getIdPartner());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
}
