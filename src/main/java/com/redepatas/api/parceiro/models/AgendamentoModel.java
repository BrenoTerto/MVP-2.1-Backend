package com.redepatas.api.parceiro.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.redepatas.api.cliente.models.PetModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "agendamento", uniqueConstraints = @UniqueConstraint(name = "uk_agendamento_horario_data", columnNames = {
        "horario_id", "data_agendamento" }))
public class AgendamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "preco_final", nullable = false)
    private Double precoFinal;

    @NotNull
    @Column(name = "data_criacao_agendamento", nullable = false)
    private LocalDateTime dataCriacaoAgendamento;

    @NotNull
    @Column(name = "data_agendamento", nullable = false)
    private LocalDate dataAgendamento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false)
    private AgendaHorarioModel horario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoModel servico;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private PartnerModel parceiro;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;

    @OneToMany
    @JoinColumn(name = "agendamento_id")
    private List<AgendamentoAdicionalModel> itens;
}
