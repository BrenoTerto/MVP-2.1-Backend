package com.redepatas.api.cliente.dtos.petDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record NewPetDto(
        String rgPet,
        @NotNull(message = "O campo nome é obrigatório.") String nome,
        @NotNull(message = "O campo espécie é obrigatório.") String especie,
        String raca,
        String observacoes,
        @NotNull(message = "O campo castrado é obrigatório.") Boolean castrado,
        Boolean sociavel,
        List<VacinaDto> vacinas, // Lista de DTOs para a entidade Vacina
        LocalDate dataNascimento,
        @NotNull(message = "O campo sexo é obrigatório.") String sexo,
        BigDecimal peso,
        String tipoSanguineo,
        @NotNull(message = "O porte do animal deve ser especificado") String porte) {
}