package com.redepatas.api.cliente.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import com.redepatas.api.parceiro.models.TipoServico;

@Data
@Entity
@Table(name = "planos_assinatura")
@NoArgsConstructor
@AllArgsConstructor
public class PlanoAssinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private BigDecimal preco;

    private int duracaoDias = 30;

    private int nivel = 1;

    private boolean customizado = false;

    @ElementCollection(targetClass = TipoServico.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "plano_servicos", joinColumns = @JoinColumn(name = "plano_id"))
    @Column(name = "tipo_servico")
    private List<TipoServico> servicos = new ArrayList<>();
}
