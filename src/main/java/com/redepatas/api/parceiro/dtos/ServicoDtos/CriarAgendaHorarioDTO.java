package com.redepatas.api.parceiro.dtos.ServicoDtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CriarAgendaHorarioDTO {

  @NotBlank(message = "Horário de início é obrigatório.")
  private String horarioInicio;

  @NotBlank(message = "Horário de fim é obrigatório.")
  private String horarioFim;
}