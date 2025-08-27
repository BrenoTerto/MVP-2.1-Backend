package com.redepatas.api.parceiro.dtos.PartnerDtos;

import java.math.BigDecimal;
import java.util.UUID;

public interface ParceiroBuscaProjecao {

  UUID getId_partner();

  UUID getId_servico();

  Integer getQuantidade_horarios();

  String getNome_do_parceiro();

  String getImagem();

  Double getAvaliacao();

  String getCidade();

  String getBairro();

  String getRua();

  String getServico_oferecido();

  BigDecimal getPreco_pequeno();

  BigDecimal getPreco_grande();

}