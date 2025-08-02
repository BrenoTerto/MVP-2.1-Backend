package com.redepatas.api.parceiro.dtos.DisponibilidadeDtos;

import java.util.List;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

public record SlotsDisponiveisDTO(
    DiaSemana diaSemana,
    List<SlotHorarioDTO> slots
) {}
