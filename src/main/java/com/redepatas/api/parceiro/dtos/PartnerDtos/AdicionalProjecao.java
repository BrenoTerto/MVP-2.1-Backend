package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.math.BigDecimal;
import java.util.UUID;

public interface AdicionalProjecao {

  UUID getId();

  String getNome();

  String getDescricao();

  BigDecimal getPreco_Pequeno();

  BigDecimal getPreco_Grande();

}