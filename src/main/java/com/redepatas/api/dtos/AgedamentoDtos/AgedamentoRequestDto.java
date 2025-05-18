package com.redepatas.api.dtos.AgedamentoDtos;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AgedamentoRequestDto(
        @NotNull(message = "O campo id do Parceiro é obrigatório.") UUID idPartner,
        @NotNull(message = "O campo id do Pet é obrigatório.") UUID idPet,
        @NotNull(message = "O campo do serviço selecionado é obrigatório.") String servico,
        @NotNull(message = "O campo de data é obrigatório.") LocalDateTime dataAgendamento

) {

}
