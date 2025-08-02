package com.redepatas.api.parceiro.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PetModel;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "agendamento")
public class AgendamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    @NotNull(message = "Serviço é obrigatório")
    private ServicoModel servico;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "Cliente é obrigatório")
    private ClientModel cliente;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    @NotNull(message = "Pet é obrigatório")
    private PetModel pet;

    @ManyToOne
    @JoinColumn(name = "parceiro_id", nullable = false)
    @NotNull(message = "Parceiro é obrigatório")
    private PartnerModel parceiro;

    @NotNull(message = "Data do agendamento é obrigatória")
    private LocalDate dataAgendamento;

    @NotNull(message = "Horário de início é obrigatório")
    private LocalTime horarioInicio;

    @NotNull(message = "Horário de fim é obrigatório")
    private LocalTime horarioFim;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status do agendamento é obrigatório")
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    private String observacoes;

    private Double valorTotal;

    private Boolean avaliado = false;

}
