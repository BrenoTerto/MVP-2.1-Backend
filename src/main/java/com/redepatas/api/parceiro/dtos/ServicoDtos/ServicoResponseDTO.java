package com.redepatas.api.parceiro.dtos.ServicoDtos;

import java.util.List;
import java.util.UUID;

import com.redepatas.api.parceiro.models.TipoServico;

import lombok.Data;

@Data
public class ServicoResponseDTO {
    
    private UUID id;
    private String nome;
    private String descricao;
    private TipoServico tipo;
    private double precoPequeno;
    private Double precoGrande;
    private Boolean aceitaPetGrande;
    private List<AdicionalResponseDTO> adicionais;
    private AgendaResponseDTO agenda;
    
    // Informações do parceiro
    private UUID parceiroId;
    private String nomeParceiro;
}
