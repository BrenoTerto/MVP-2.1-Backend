package com.redepatas.api.parceiro.dtos.AgedamentoDtos;

import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

public record ResponseAgendamentosdto(
        UUID idAgendamento,
        StatusAgendamento status,
        Boolean avaliado,
        String servico,
        String nomePet,
        String racaPet,
        String dataAgendamento,
        String horarioEscolhido,
        String nomeClinica,
        String cpfCnpj) {
}
