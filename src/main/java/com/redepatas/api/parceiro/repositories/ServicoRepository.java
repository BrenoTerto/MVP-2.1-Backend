package com.redepatas.api.parceiro.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.ServicoModel;
import com.redepatas.api.parceiro.models.TipoServico;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoModel, UUID> {

    boolean existsByTipo(TipoServico tipo);

    List<ServicoModel> findByParceiroIdPartner(UUID parceiroId);

    boolean existsByTipoAndParceiro(TipoServico tipo, PartnerModel parceiro);

}