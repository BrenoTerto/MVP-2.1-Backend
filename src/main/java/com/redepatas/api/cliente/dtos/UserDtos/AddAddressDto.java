package com.redepatas.api.cliente.dtos.UserDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddAddressDto(
        @NotBlank(message = "O campo rua é obrigatório.") @Size(max = 100) String rua,
        @NotBlank(message = "O campo bairro é obrigatório.") @Size(max = 50) String bairro,
        @NotBlank(message = "O campo cidade é obrigatório.") @Size(max = 50) String cidade,
        @NotBlank(message = "O campo estado é obrigatório.") @Size(max = 50) String estado,
        @NotBlank(message = "O campo CEP é obrigatório.") @Size(max = 10) String cep,
        @NotNull(message = "O campo número é obrigatório.") Integer numero,
        @NotBlank(message = "O campo complemento é obrigatório.") @Size(max = 50) String complemento,
        @NotBlank(message = "O campo lugar é obrigatório.") @Size(max = 50) String lugar) {
}
