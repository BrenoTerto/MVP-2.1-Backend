package com.redepatas.api.dtos;

import jakarta.validation.constraints.NotNull;

public record RecoveryPasswordDto(
                @NotNull(message = "O campo Hash é obrigatório") String hash,
                @NotNull(message = "O campo da nova senha é obrigatório.") String newPassword) {

}
