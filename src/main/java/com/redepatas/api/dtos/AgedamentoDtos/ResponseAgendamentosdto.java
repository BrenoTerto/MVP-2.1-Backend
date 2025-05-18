package com.redepatas.api.dtos.AgedamentoDtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.redepatas.api.models.Enum.StatusAgendamento;

public record ResponseAgendamentosdto(
    UUID idAgendamento,
    StatusAgendamento status,
    Boolean avaliado,
    String servico,
    String nomePet,
    String racaPet,
    LocalDateTime dataAgendamento,
    String nomeClinica,
    String cpfCnpj
) {}
