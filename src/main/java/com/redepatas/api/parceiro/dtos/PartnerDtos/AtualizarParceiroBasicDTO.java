package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.constraints.Size;

public record AtualizarParceiroBasicDTO(
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
    String name,

    @Size(max = 1000, message = "A descrição pode ter no máximo 1000 caracteres")
    String descricao
) {}

