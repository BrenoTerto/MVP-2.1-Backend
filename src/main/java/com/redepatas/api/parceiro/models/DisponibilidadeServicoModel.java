package com.redepatas.api.parceiro.models;

import java.time.LocalTime;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

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
@Table(name = "disponibilidade_servico")
public class DisponibilidadeServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    @NotNull(message = "Serviço é obrigatório")
    private ServicoModel servico;

    @ManyToOne
    @JoinColumn(name = "parceiro_id", nullable = false)
    @NotNull(message = "Parceiro é obrigatório")
    private PartnerModel parceiro;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Dia da semana é obrigatório")
    private DiaSemana diaSemana;

    @NotNull(message = "Horário de início é obrigatório")
    private LocalTime horarioInicio;

    @NotNull(message = "Horário de fim é obrigatório")
    private LocalTime horarioFim;

    @NotNull(message = "Duração do slot em minutos é obrigatória")
    private Integer duracaoSlotMinutos = 60;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo = true;

}
