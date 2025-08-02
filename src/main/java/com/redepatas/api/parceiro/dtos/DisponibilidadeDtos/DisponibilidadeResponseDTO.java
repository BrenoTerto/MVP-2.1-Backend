package com.redepatas.api.parceiro.dtos.DisponibilidadeDtos;

import java.time.LocalTime;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

public record DisponibilidadeResponseDTO(
    UUID id,
    DiaSemana diaSemana,
    LocalTime horarioInicio,
    LocalTime horarioFim,
    Integer duracaoSlotMinutos,
    Boolean ativo,
    String nomeServico
) {}
