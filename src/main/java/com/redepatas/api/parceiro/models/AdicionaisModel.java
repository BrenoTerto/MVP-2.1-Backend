package com.redepatas.api.parceiro.models;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "adicionais")
public class AdicionaisModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Nome do adicional é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço pequeno não pode ser nulo")
    @Positive(message = "Preço pequeno deve ser maior que zero")
    private double precoPequeno;
    
    private Double precoGrande;
    
    @NotNull(message = "Indicação se aceita pet grande é obrigatória")
    private Boolean aceitaPetGrande = true;
}
