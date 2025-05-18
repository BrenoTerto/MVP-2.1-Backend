package com.redepatas.api.dtos.AssinaturaDtos;

import java.math.BigDecimal;
import java.util.List;

public record PlanoDto(
    Long id,
    String nome,
    BigDecimal preco,
    List<String> beneficios,
    int duracaoDias
) {}
