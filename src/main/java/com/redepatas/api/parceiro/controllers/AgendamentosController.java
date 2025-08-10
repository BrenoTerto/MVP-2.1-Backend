package com.redepatas.api.parceiro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.CriarAgendamentoDTO;
import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.parceiro.services.AgendamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/agendamentos")
@Validated
public class AgendamentosController {

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody CriarAgendamentoDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof ClientModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como cliente");
            }

            ClientModel clienteLogado = (ClientModel) authentication.getPrincipal();

            AgendamentoResponseDTO resp = agendamentoService.criarAgendamento(dto, clienteLogado.getIdUser());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
}
