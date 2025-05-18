package com.redepatas.api.dtos.PartnerDtos;

import jakarta.validation.constraints.NotNull;

public record getPartnerDtos(
        @NotNull(message = "Obrigatório definir o porte do animal!") String porte,
        @NotNull(message = "Obrigatório definir o serviço selecionado") String nomeServico,
        String cidade,
        String rua,
        String bairro) {
}
