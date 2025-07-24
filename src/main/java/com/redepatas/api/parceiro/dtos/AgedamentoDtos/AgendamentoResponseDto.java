package com.redepatas.api.parceiro.dtos.AgedamentoDtos;

import java.time.LocalDate;
import java.util.UUID;

import com.redepatas.api.cliente.dtos.petDtos.GetPetsDto;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

public record AgendamentoResponseDto(
        UUID id,
        LocalDate dataAgendada,
        String horarioEscolhido,
        StatusAgendamento status,
        ClientAgendamentoDto cliente,
        GetPetsDto pet) {
}
