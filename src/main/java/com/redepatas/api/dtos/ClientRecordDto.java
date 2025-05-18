package com.redepatas.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ClientRecordDto(@NotBlank String name, @NotNull String login, @NotNull String password) {
}
