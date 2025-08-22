package com.redepatas.api.parceiro.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PetModel;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false)
    private StatusAgendamento status;

    private Boolean avaliado;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Tipo do serviço é obrigatório")
    private TipoServico servico_tipo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private PartnerModel parceiro;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClientModel cliente;

    // DADOS SNAPSHOT DO PET
    private String pet_avatarUrl;
    @NotNull(message = "O campo nome é obrigatório.")
    @Column(nullable = false)
    private String pet_nome;
    @NotNull(message = "O campo espécie é obrigatório.")
    @Column(nullable = false)
    private String pet_especie;
    private String pet_raca;
    private String pet_observacoes;
    @NotNull(message = "O campo castrado é obrigatório.")
    @Column(nullable = false)
    private Boolean pet_castrado;
    private Boolean pet_sociavel;
    private String pet_sexo;
    private BigDecimal pet_peso;
    private String pet_tipoSanguineo;
    private String pet_porte;

    @OneToMany
    @JoinColumn(name = "agendamento_id")
    private List<AgendamentoAdicionalModel> itens;
}
