package com.redepatas.api.parceiro.dtos.ServicoDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CriarAgendaDTO {

  @Valid
  @NotEmpty(message = "A agenda deve conter pelo menos um dia.")
  private List<CriarAgendaDiaDTO> dias;
}