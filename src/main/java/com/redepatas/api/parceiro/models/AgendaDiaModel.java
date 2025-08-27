package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

import com.redepatas.api.parceiro.models.Enum.DiaSemana;

@Data
@Entity
@Table(name = "agenda_dia")
public class AgendaDiaModel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Enumerated(EnumType.STRING)
  private DiaSemana diaSemana;

  @ManyToOne
  @JoinColumn(name = "agenda_id")
  private AgendaModel agenda;

  @OneToMany(mappedBy = "dia", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AgendaHorarioModel> horarios = new ArrayList<>();

}