package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarEnderecoPartnerDTO(
    String rua,
    String bairro,
    String cidade,
    String estado,
    String cep,
    Integer numero,
    String complemento,
    String lugar
) {}

