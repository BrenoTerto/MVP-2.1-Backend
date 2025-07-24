package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record GetPartnerServiceDto(
        @NotNull(message = "O campo ID Parceiro é obrigatório.") UUID idParceiro,
        @NotNull(message = "O nome do serviço é obrigatório.") String nomeServico,
        @NotNull(message = "O campo data é obrigatório.") String data) {
}
