package com.redepatas.api.parceiro.dtos.DisponibilidadeDtos;

import java.time.LocalTime;

public record SlotHorarioDTO(
    LocalTime horarioInicio,
    LocalTime horarioFim,
    Boolean disponivel
) {}
