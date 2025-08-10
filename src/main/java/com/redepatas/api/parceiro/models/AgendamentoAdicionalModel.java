package com.redepatas.api.parceiro.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "agendamento_adicional")
public class AgendamentoAdicionalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Snapshots para histórico
    @Column(name = "nome_snapshot", nullable = false)
    private String nomeSnapshot;

    @Column(name = "preco_pequeno_snapshot")
    private Double precoPequenoSnapshot;

    @Column(name = "preco_grande_snapshot")
    private Double precoGrandeSnapshot;

    @Column(name = "preco_aplicado", nullable = false)
    private Double precoAplicado;
}
