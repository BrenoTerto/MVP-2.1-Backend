package com.redepatas.api.dtos.AgedamentoDtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.redepatas.api.dtos.petDtos.GetPetsDto;
import com.redepatas.api.models.Enum.StatusAgendamento;

public record AgendamentoResponseDto(
    UUID id,
    LocalDateTime dataAgendada,
    StatusAgendamento status,
    ClientAgendamentoDto cliente,
    GetPetsDto pet
) {}
