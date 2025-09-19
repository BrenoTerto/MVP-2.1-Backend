package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.util.UUID;

public record PartnerProfileDto(
    UUID idPartner,
    String name,
    String imageUrl,
    String emailContato,
    String numeroContato,
    String descricao,
    EnderecoPartnerResponseDto endereco
) {}
