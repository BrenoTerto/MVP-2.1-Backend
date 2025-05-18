package com.redepatas.api.models.Partner;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import com.redepatas.api.models.Enum.DiaSemana;

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

    private String horarioInicio;
    private String horarioFim;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private PartnerModel partner;
}
