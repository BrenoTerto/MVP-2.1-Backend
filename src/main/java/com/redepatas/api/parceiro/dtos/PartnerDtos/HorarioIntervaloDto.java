package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.UUID;

public record HorarioIntervaloDto(
    UUID idIntervalo,
    String horarioInicio,
    String horarioFim,
    Boolean reservado) {
}