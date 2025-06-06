package com.redepatas.api.dtos.AgedamentoDtos;

import java.time.LocalDate;
import java.util.UUID;

import com.redepatas.api.models.Enum.StatusAgendamento;

public record ResponseAgendamentosdto(
        UUID idAgendamento,
        StatusAgendamento status,
        Boolean avaliado,
        String servico,
        String nomePet,
        String racaPet,
        LocalDate dataAgendamento,
        String horarioEscolhido,
        String nomeClinica,
        String cpfCnpj) {
}
