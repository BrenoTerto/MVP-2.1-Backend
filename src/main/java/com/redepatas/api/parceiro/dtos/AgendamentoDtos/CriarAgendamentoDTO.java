package com.redepatas.api.parceiro.dtos.AgendamentoDtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CriarAgendamentoDTO {
    @NotNull
    private UUID servicoId;

    @NotNull
    private UUID petId;

    @NotNull
    private UUID horarioId;

    @NotNull
    private LocalDate dataAgendamento;

    private List<UUID> adicionaisIds;
}
