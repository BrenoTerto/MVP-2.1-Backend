package com.redepatas.api.dtos.PartnerDtos;

import com.redepatas.api.models.Enum.DiaSemana;

public record HorarioFuncionamentoDto(
    DiaSemana dia,
    String horarioInicio,
    String horarioFim
) {}
