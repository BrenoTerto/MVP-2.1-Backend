package com.redepatas.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @NotNull(message = "O campo login é obrigatório.") String login,
        @NotNull(message = "O campo senha é obrigatório.") String password,
        @NotNull(message = "O campo CPF é obrigatório.") @Size(min = 14, max = 14, message = "O CPF deve conter exatamente 11 caracteres.") String CPF,
        @NotNull(message = "O campo nome é obrigatório.") String name) {
}