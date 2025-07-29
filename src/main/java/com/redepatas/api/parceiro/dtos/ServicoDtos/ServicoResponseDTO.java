package com.redepatas.api.parceiro.dtos.ServicoDtos;

import java.util.UUID;

import com.redepatas.api.parceiro.models.TipoServico;

import lombok.Data;

@Data
public class ServicoResponseDTO {
    
    private UUID id;
    private String nome;
    private TipoServico tipo;
    private double precoPequeno;
    private double precoGrande;
}
