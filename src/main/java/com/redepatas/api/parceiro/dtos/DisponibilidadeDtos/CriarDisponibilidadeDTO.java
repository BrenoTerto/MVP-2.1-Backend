package com.redepatas.api.parceiro.dtos.DisponibilidadeDtos;

import java.time.LocalTime;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarDisponibilidadeDTO(
    @NotNull(message = "Dia da semana é obrigatório")
    DiaSemana diaSemana,
    
    @NotNull(message = "Horário de início é obrigatório")
    LocalTime horarioInicio,
    
    @NotNull(message = "Horário de fim é obrigatório")
    LocalTime horarioFim,
    
    @NotNull(message = "Duração do slot é obrigatória")
    @Positive(message = "Duração do slot deve ser maior que zero")
    Integer duracaoSlotMinutos
) {}
