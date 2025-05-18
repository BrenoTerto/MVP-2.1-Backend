package com.redepatas.api.dtos.petDtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record VacinaDto(
    @NotNull(message = "O campo nome é obrigatório.") String nome,
    @NotNull(message = "O campo data Aplicação é obrigatório.") LocalDate dataAplicacao
) {}