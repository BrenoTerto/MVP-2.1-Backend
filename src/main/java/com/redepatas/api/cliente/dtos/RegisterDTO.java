package com.redepatas.api.cliente.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
    @NotNull(message = "O campo CPF é obrigatório.") String login,

    @NotNull(message = "O campo CPF é obrigatório.") @Size(min = 14, max = 14, message = "O CPF deve conter exatamente 14 caracteres.") String CPF,

    @NotNull(message = "O campo senha é obrigatório.") String password,

    @NotNull(message = "O campo nome é obrigatório.") String name,

    @NotNull(message = "O campo número de telefone é obrigatório.") String numero,

    @NotNull(message = "Campo obrigatório") LocalDate dataNascimento,
    
    @NotNull(message = "O campo e-mail é obrigatório.") @Email(message = "O e-mail informado não é válido.") String email) {
}
