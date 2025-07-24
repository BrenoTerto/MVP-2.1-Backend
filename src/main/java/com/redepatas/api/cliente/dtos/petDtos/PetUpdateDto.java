package com.redepatas.api.cliente.dtos.petDtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PetUpdateDto(
        String avatarUrl,
        @NotNull(message = "O campo nome é obrigatório.") String nome,
        @NotNull(message = "O campo espécie é obrigatório.") String especie,
        String raca,
        String observacoes,
        @NotNull(message = "O campo castrado é obrigatório.") Boolean castrado,
        Boolean sociavel,
        LocalDate dataNascimento,
        @NotNull(message = "O campo sexo é obrigatório.") String sexo,
        BigDecimal peso,
        String tipoSanguineo,
        String porte) {
}