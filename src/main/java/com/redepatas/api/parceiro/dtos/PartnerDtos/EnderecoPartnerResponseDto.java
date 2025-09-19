package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.UUID;

public record EnderecoPartnerResponseDto(
        UUID idEndereco,
        String rua,
        Integer numero,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String complemento,
        String lugar) {
}
