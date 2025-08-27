package com.redepatas.api.parceiro.dtos.PartnerDtos;

public record AtualizarEnderecoPartnerDTO(
        String rua,
        String bairro,
        String cidade,
        String estado,
        String cep,
        Integer numero,
        String complemento,
        String lugar) {
}
