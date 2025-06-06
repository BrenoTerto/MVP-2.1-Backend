package com.redepatas.api.dtos.UserDtos;

public record EnderecoDto(
    String idEndereco,
    String rua,
    String cidade,
    String estado,
    String bairro,
    String cep,
    String complemento,
    Integer numero,
    String lugar,
    Boolean selecionado
) {}
