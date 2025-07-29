package com.redepatas.api.parceiro.dtos.ServicoDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CriarServicoDTO {
    
    @NotBlank(message = "Nome do serviço é obrigatório")
    private String nome;
    
    @NotBlank(message = "Tipo do serviço é obrigatório")
    private String tipo; // Será validado se é BANHO, TOSA ou CONSULTA
    
    @NotNull(message = "Preço pequeno é obrigatório")
    @Positive(message = "Preço pequeno deve ser maior que zero")
    private Double precoPequeno;
    
    @Positive(message = "Preço grande deve ser maior que zero")
    private Double precoGrande;
}
