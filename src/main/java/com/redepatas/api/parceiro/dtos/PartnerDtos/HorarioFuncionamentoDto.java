package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.List;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

public record HorarioFuncionamentoDto(
    DiaSemana dia,
    List<HorarioIntervaloDto> intervalos) {
}
