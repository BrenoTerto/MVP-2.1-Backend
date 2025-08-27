package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.constraints.NotNull;

public record BuscarParceirosDisponiveisDTO(
                String cidade,
                String rua,
                String bairro,
                @NotNull(message = "Campo diaSemana obrigatório") String diaSemana,
                @NotNull(message = "Campo tipoServico obrigatório") String tipoServico,
                @NotNull(message = "Campo tamanhoPet obrigatório") String tamanhoPet) {

}