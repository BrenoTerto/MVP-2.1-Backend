package com.redepatas.api.dtos.AgedamentoDtos;

import java.time.LocalDate;
import java.util.UUID;

import com.redepatas.api.dtos.petDtos.GetPetsDto;
import com.redepatas.api.models.Enum.StatusAgendamento;

public record AgendamentoResponseDto(
        UUID id,
        LocalDate dataAgendada,
        String horarioEscolhido,
        StatusAgendamento status,
        ClientAgendamentoDto cliente,
        GetPetsDto pet) {
}
