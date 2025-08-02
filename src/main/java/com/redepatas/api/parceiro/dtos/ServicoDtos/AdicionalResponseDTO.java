package com.redepatas.api.parceiro.dtos.ServicoDtos;

import java.util.UUID;

import lombok.Data;

@Data
public class AdicionalResponseDTO {
    
    private UUID id;
    private String nome;
    private String descricao;
    private double precoPequeno;
    private Double precoGrande;
    private Boolean aceitaPetGrande;
}
