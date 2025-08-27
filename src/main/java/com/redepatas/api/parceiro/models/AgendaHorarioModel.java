package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "agenda_horario")
public class AgendaHorarioModel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String horarioInicio;
  private String horarioFim;

  @ManyToOne
  @JoinColumn(name = "dia_id")
  private AgendaDiaModel dia;

  // getters e setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getHorarioInicio() {
    return horarioInicio;
  }

  public void setHorarioInicio(String horarioInicio) {
    this.horarioInicio = horarioInicio;
  }

  public String getHorarioFim() {
    return horarioFim;
  }

  public void setHorarioFim(String horarioFim) {
    this.horarioFim = horarioFim;
  }

  public AgendaDiaModel getDia() {
    return dia;
  }

  public void setDia(AgendaDiaModel dia) {
    this.dia = dia;
  }
}