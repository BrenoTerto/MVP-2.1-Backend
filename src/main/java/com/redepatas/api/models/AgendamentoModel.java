package com.redepatas.api.models;

import java.time.LocalDate;
import java.util.UUID;

import com.redepatas.api.models.Enum.StatusAgendamento;
import com.redepatas.api.models.Partner.HorarioIntervaloModel;
import com.redepatas.api.models.Partner.PartnerModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "agendamentos")
public class AgendamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private ClientModel cliente;

    @ManyToOne
    private PartnerModel partnerModel;

    @ManyToOne
    private PetModel petModel;

    private LocalDate dataAgendamento;
    @ManyToOne
    private HorarioIntervaloModel intervalo;

    private StatusAgendamento statusAgendamento;
    private Boolean avaliado;
    private String servico;

    public AgendamentoModel(ClientModel cliente, PartnerModel partnerModel, PetModel petModel,
            LocalDate dataAgendamento, HorarioIntervaloModel hIntervaloModel, StatusAgendamento statusAgendamento,
            String servico) {
        this.cliente = cliente;
        this.partnerModel = partnerModel;
        this.petModel = petModel;
        this.dataAgendamento = dataAgendamento;
        this.intervalo = hIntervaloModel;
        this.statusAgendamento = statusAgendamento;
        this.avaliado = false;
        this.servico = servico;
    }
}
