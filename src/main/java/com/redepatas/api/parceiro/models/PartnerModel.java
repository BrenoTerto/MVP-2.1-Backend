package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.TipoPartner;

@Table(name = "partner")
@Entity(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPartner")
public class PartnerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idPartner;

    private String name;

    private String imageUrl;

    private String cnpjCpf;

    private Long avaliacao;
    private Integer qtdAvaliacoes;

    private String emailContato;
    private String numeroContato;

    private String tipoPet;

    @OneToOne(mappedBy = "partnerModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private EnderecoPartner endereco;

    @Enumerated(EnumType.STRING)
    private TipoPartner tipo;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Servico> servicos;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioFuncionamentoModel> horariosFuncionamento;

    private String descricao;

    public PartnerModel(
            String name,
            String imageUrl,
            String cnpjCpf,
            String emailContato,
            String numeroContato,
            EnderecoPartner endereco,
            TipoPartner tipo,
            String descricao,
            String tipoPet, // TODOS, GRANDE, PEQUENO
            List<Servico> servicos,
            List<HorarioFuncionamentoModel> horariosFuncionamento) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.cnpjCpf = cnpjCpf;
        this.avaliacao = 5L;
        this.qtdAvaliacoes = 1;
        this.emailContato = emailContato;
        this.numeroContato = numeroContato;
        this.endereco = endereco;
        this.tipo = tipo;
        this.tipoPet = tipoPet;
        this.descricao = descricao;
        this.servicos = servicos;
        this.horariosFuncionamento = horariosFuncionamento;
    }
}
