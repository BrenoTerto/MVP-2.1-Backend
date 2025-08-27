package com.redepatas.api.cliente.dtos;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordDto(@NotNull(message = "O campo da senha antiga é obrigatório.") String oldPassword,
    @NotNull(message = "O campo da nova senha é obrigatório.") String newPassword) {

}
