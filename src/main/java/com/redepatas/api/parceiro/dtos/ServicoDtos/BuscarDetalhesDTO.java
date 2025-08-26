package com.redepatas.api.parceiro.dtos.ServicoDtos;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record BuscarDetalhesDTO(
    @NotNull(message = "Campo obrigatorio e não vazio") UUID idServico,
    @NotNull(message = "Campo obrigatorio e não vazio") LocalDate dataAgendamento
) {
    
}
