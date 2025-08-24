package com.redepatas.api.cliente.dtos.UserDtos;

import java.time.LocalDate;

public record UpdateProfileDto(
                String nome,
                String telefone,
                LocalDate dataNascimento) {
}