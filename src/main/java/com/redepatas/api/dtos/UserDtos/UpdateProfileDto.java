package com.redepatas.api.dtos.UserDtos;

import java.time.LocalDate;

public record UpdateProfileDto(
        String name,
        String CPF,
        LocalDate birthDate) {
}