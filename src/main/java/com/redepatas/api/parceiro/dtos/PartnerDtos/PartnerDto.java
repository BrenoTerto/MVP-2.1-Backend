package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.UUID;

public record PartnerDto(
    UUID idParceiro,
    UUID idServico,
    Integer quantidadeHorarios,
    String imageUrl,
    String name,
    String descricao,
    Long precoPequeno,
    Long precoGrande,
    Long avaliacao,
    EnderecoParteDto endereco,
    String distancia,
    String tempo) {
}
