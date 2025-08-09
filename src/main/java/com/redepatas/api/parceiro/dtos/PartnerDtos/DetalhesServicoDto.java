package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.List;

public record DetalhesServicoDto(
                List<AdicionalProjecao> adicionais,
                List<HorarioProjecao> horarios) {
}
