package com.redepatas.api.dtos.UserDtos;

import java.time.LocalDate;
import java.util.List;


public record ClientResponseDTO(
    String photoUrl,
    String CPF,
    String nome,
    LocalDate dataNascimento,
    String login,
    String email,
    String telefone,
    List<EnderecoDto> enderecos
) {}
