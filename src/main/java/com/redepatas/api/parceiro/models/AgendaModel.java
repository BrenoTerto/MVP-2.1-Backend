package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Data
@Entity
@Table(name = "agenda")
public class AgendaModel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "servico_id")
  private ServicoModel servico;

  @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AgendaDiaModel> dias = new ArrayList<>();

}