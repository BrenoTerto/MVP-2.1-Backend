package com.redepatas.api.dtos.PartnerDtos;

import java.util.List;

public record PartenerServicesDto(
        ServicoDto servico,
        List<HorarioFuncionamentoDto> horarioFuncionamento) {
}
