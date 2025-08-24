package com.redepatas.api.parceiro.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PartnerPasswordResetDtos {

    public record PartnerResetRequestDto(
            @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email) {
    }

    public record PartnerResetConfirmDto(
            @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
            @NotBlank(message = "Token é obrigatório") String token,
            @NotBlank(message = "Nova senha é obrigatória") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") String newPassword) {
    }
}
