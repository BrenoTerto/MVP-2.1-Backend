package com.redepatas.api.dtos.PartnerDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ServicoDto(
    @NotBlank (message = "É necessário definnir o nome do serviço!")String nome,
    @NotNull (message = "É preciso definir o preço do serviço!") Double price,
    @NotNull @Size(min = 1, message = "Cada serviço deve ter pelo menos um subserviço.")
    List<@Valid SubServicoDto> subServicos
) {}
