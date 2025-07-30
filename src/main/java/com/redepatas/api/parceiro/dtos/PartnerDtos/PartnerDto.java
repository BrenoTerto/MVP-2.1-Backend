package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.UUID;

import com.redepatas.api.cliente.dtos.UserDtos.EnderecoDto;

public record PartnerDto(
                UUID id,
                String imageUrl,
                String name,
                String email,
                String descricao,
                Long avaliacao,
                EnderecoDto endereco) {

}
