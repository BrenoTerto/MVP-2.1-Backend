package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.List;

public record PartenerServicesDto(
                ServicoDto servico,
                List<HorarioFuncionamentoDto> horarioFuncionamento) {
}
