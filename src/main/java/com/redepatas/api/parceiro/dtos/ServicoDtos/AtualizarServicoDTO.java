package com.redepatas.api.parceiro.dtos.ServicoDtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class AtualizarServicoDTO {

    private String nome;
    private String descricao;

    @Positive(message = "Preço pequeno deve ser maior que zero")
    private Double precoPequeno;

    private Double precoGrande;

    private Boolean aceitaPetGrande;
}
