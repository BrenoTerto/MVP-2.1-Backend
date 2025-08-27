package com.redepatas.api.parceiro.dtos.AgedamentoDtos;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliarServicoDto(
                @NotNull(message = "ID do agendamento é obrigatório") UUID idAgendamento,
                @Min(value = 1, message = "A nota mínima é 1") @Max(value = 5, message = "A nota máxima é 5") @NotNull(message = "O campo nota é obrigatório") Integer nota) {

}
