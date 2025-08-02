package com.redepatas.api.parceiro.models;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "servico")
public class ServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Nome do serviço é obrigatório")
    private String nome;

    private String descricao;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Tipo do serviço é obrigatório")
    private TipoServico tipo;

    @NotNull(message = "O preço pequeno não pode ser nulo")
    @Positive(message = "Preço deve ser maior que zero")
    private double precoPequeno;

    private Double precoGrande;
    
    @NotNull(message = "Indicação se aceita pet grande é obrigatória")
    private Boolean aceitaPetGrande = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    @NotNull(message = "Parceiro é obrigatório")
    private PartnerModel parceiro;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "servico_id")
    private List<AdicionaisModel> adicionais;

}
