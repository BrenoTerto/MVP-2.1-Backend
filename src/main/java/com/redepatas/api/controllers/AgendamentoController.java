package com.redepatas.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redepatas.api.dtos.AgedamentoDtos.AgedamentoRequestDto;
import com.redepatas.api.dtos.AgedamentoDtos.AgendamentoResponseDto;
import com.redepatas.api.dtos.AgedamentoDtos.AvaliarServicoDto;
import com.redepatas.api.dtos.AgedamentoDtos.ResponseAgendamentosdto;
import com.redepatas.api.services.AgendamentoService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    AgendamentoService agendamentoService;

    @PostMapping("/criarAgendamento")
    public ResponseEntity<String> criarAgendamento(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AgedamentoRequestDto dto) throws MessagingException {

        String login = userDetails.getUsername();

        String resultado = agendamentoService.criarAgendamento(
                login,
                dto.idPartner(),
                dto.idPet(),
                dto.servico(),
                dto.dataAgendamento());

        if (resultado.contains("Falha")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/getAgendamento/{id}")
    public ResponseEntity<AgendamentoResponseDto> buscarAgendamentoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @PutMapping("/avaliar")
    public ResponseEntity<String> avaliarServico(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvaliarServicoDto avaliarServicoDto) {
        String login = userDetails.getUsername();
        return ResponseEntity.ok(agendamentoService.avaliarServico(login, avaliarServicoDto));
    }

    @GetMapping("/meusAgendamentos")
    public ResponseEntity<List<ResponseAgendamentosdto>> meusAgendamentos(
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        return ResponseEntity.ok(agendamentoService.meusAgendamentos(login));
    }

}
