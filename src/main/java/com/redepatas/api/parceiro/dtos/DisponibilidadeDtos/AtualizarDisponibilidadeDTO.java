package com.redepatas.api.parceiro.dtos.DisponibilidadeDtos;

import java.time.LocalTime;

import jakarta.validation.constraints.Positive;

public record AtualizarDisponibilidadeDTO(
    LocalTime horarioInicio,
    LocalTime horarioFim,
    @Positive(message = "Duração do slot deve ser maior que zero")
    Integer duracaoSlotMinutos,
    Boolean ativo
) {}
