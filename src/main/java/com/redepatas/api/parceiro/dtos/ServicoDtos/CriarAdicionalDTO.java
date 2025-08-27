package com.redepatas.api.parceiro.dtos.ServicoDtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CriarAdicionalDTO {
    
    @NotBlank(message = "Nome do adicional é obrigatório")
    private String nome;
    
    private String descricao;
    
    @NotNull(message = "Preço pequeno é obrigatório")
    @Positive(message = "Preço pequeno deve ser maior que zero")
    private Double precoPequeno;
    
    private Double precoGrande;
}
