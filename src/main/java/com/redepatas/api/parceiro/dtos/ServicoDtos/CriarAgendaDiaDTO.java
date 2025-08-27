package com.redepatas.api.parceiro.dtos.ServicoDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CriarAgendaDiaDTO {

  @NotNull(message = "O dia da semana é obrigatório.")
  private String diaSemana; // Ex: "SEGUNDA", "TERCA", etc.

  @Valid
  @NotEmpty(message = "Deve haver pelo menos um horário para o dia.")
  private List<CriarAgendaHorarioDTO> horarios;
}