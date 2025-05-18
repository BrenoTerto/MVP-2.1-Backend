package com.redepatas.api.dtos.AssinaturaDtos;

import java.time.LocalDate;
import java.util.UUID;

public record AssinaturaAtivaDto(
    UUID id,
    LocalDate dataInicio,
    LocalDate dataFim,
    String status,
    PlanoDto plano
) {}
