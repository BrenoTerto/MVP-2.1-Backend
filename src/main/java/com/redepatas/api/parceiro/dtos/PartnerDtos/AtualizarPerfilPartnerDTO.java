package com.redepatas.api.parceiro.dtos.PartnerDtos;

import com.redepatas.api.parceiro.models.Enum.TipoPartner;
import com.redepatas.api.parceiro.models.Enum.TipoPet;

import jakarta.validation.constraints.Size;

public record AtualizarPerfilPartnerDTO(
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String name,

        @Size(max = 1000, message = "A descrição pode ter no máximo 1000 caracteres")
        String descricao,

        @Size(min = 8, max = 20, message = "O número de contato deve ter entre 8 e 20 caracteres")
        String numeroContato,

        TipoPet tipoPet,

        TipoPartner categoria,

        AtualizarEnderecoPartnerDTO endereco
) {}
