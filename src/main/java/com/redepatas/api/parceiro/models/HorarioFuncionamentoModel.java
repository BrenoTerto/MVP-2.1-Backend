package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

@Entity
@Table(name = "horario_funcionamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HorarioFuncionamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private DiaSemana dia;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private PartnerModel partner;

    @OneToMany(mappedBy = "horarioFuncionamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioIntervaloModel> intervalos;
}
