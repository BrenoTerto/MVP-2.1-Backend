package com.redepatas.api.cliente.dtos.AssinaturaDtos;

import java.math.BigDecimal;
import java.util.List;

import com.redepatas.api.parceiro.models.TipoServico;

public record PlanoDto(
        Long id,
        String nome,
        BigDecimal preco,
        List<TipoServico> beneficios,
        int duracaoDias) {
}
