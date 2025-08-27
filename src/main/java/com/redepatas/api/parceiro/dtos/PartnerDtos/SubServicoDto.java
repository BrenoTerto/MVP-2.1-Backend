package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubServicoDto(
                @NotBlank(message = "Definir o nome do subserviço é obrigatório!") String nome,
                String descricao,
                @NotNull(message = "Definir o preço é obrigatório!") Double preco) {
}
