package com.redepatas.api.models.Partner;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horario_intervalo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class HorarioIntervaloModel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String horarioInicio;
  private String horarioFim;

  @ManyToOne
  @JoinColumn(name = "horario_funcionamento_id")
  private HorarioFuncionamentoModel horarioFuncionamento;
}