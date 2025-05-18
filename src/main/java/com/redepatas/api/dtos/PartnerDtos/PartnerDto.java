package com.redepatas.api.dtos.PartnerDtos;

import java.util.UUID;


import com.redepatas.api.dtos.UserDtos.EnderecoDto;

public record PartnerDto(
        UUID id,
        String imageUrl,
        String name,
        String email,
        Double avaliacao,
        EnderecoDto endereco,
        String distancia,
        String tempo,
        Double price) {

}
