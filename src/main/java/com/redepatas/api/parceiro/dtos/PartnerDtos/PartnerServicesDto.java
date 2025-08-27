package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.List;

public record PartnerServicesDto(
                ServicoDto servico,
                List<HorarioFuncionamentoDto> horarioFuncionamento) {
}
