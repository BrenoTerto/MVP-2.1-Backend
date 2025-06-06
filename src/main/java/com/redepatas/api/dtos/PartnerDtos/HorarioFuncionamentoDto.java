package com.redepatas.api.dtos.PartnerDtos;

import java.util.List;

import com.redepatas.api.models.Enum.DiaSemana;

public record HorarioFuncionamentoDto(
        DiaSemana dia,
        List<HorarioIntervaloDto> intervalos) {
}
