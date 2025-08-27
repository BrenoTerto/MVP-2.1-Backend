package com.redepatas.api.cliente.dtos.petDtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeletePetDto(
        @NotNull UUID petId,
        @NotNull UUID vacinaId) {
}
