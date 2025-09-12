package com.redepatas.api.parceiro.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.parceiro.dtos.AgedamentoDtos.AvaliarServicoDto;
import com.redepatas.api.parceiro.dtos.AgedamentoDtos.ResponseAgendamentosdto;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoDetalheResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AtualizarStatusAgendamentoDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.CriarAgendamentoDTO;
import com.redepatas.api.parceiro.models.PartnerModel;
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

    @GetMapping("/hoje")
    public ResponseEntity<?> listarAgendamentosDoDia(@RequestParam(value = "data", required = false) String dataStr) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }
            PartnerModel parceiro = (PartnerModel) authentication.getPrincipal();

            java.time.LocalDate data = dataStr == null || dataStr.isBlank()
                    ? java.time.LocalDate.now()
                    : java.time.LocalDate.parse(dataStr);
            var lista = agendamentoService.listarAgendamentosDoDia(parceiro.getIdPartner(), data);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PutMapping("/avaliar")
    public ResponseEntity<String> avaliarServico(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvaliarServicoDto avaliarServicoDto) {
        String login = userDetails.getUsername();
        return ResponseEntity.ok(agendamentoService.avaliarServico(login, avaliarServicoDto));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarAgendamentosPendentes() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }
            PartnerModel parceiro = (PartnerModel) authentication.getPrincipal();
            var lista = agendamentoService.listarAgendamentosPendentes(parceiro.getIdPartner());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDetalheResponseDTO> detalhesAgendamento(@PathVariable java.util.UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                throw new IllegalArgumentException("Acesso negado: usuário não autenticado como parceiro");
            }
            PartnerModel parceiro = (PartnerModel) authentication.getPrincipal();
            return ResponseEntity.ok(agendamentoService.buscarDetalhesAgendamento(parceiro.getIdPartner(), id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/decisao")
    public ResponseEntity<?> decidirAgendamento(@PathVariable java.util.UUID id,
            @Valid @RequestBody AtualizarStatusAgendamentoDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof PartnerModel)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Acesso negado: usuário não autenticado como parceiro");
            }
            PartnerModel parceiro = (PartnerModel) authentication.getPrincipal();
            agendamentoService.decidirAgendamento(parceiro.getIdPartner(), id, dto.getAceitar());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @GetMapping({"/meusAgendamentos"})
    public ResponseEntity<List<ResponseAgendamentosdto>> meusAgendamentos(
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        return ResponseEntity.ok(this.agendamentoService.meusAgendamentos(login));
    }

}
