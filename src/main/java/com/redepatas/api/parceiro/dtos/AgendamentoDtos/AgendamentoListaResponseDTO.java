package com.redepatas.api.parceiro.dtos.AgendamentoDtos;

import java.time.LocalDate;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;
import com.redepatas.api.parceiro.models.TipoServico;

public record AgendamentoListaResponseDTO(
    UUID id,
    LocalDate dataAgendamento,
    String horario,
    StatusAgendamento status,
    String clienteNome,
    String petNome,
    Double precoFinal,
    TipoServico servicoTipo
) {}

