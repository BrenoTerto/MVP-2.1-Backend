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

    List<ServicoModel> findByTipo(TipoServico tipo);
    
    List<ServicoModel> findByAceitaPetGrande(Boolean aceitaPetGrande);
    
    List<ServicoModel> findByTipoAndAceitaPetGrande(TipoServico tipo, Boolean aceitaPetGrande);
    
    boolean existsByNomeAndTipo(String nome, TipoServico tipo);
    
    // Métodos específicos por parceiro
    List<ServicoModel> findByParceiro(PartnerModel parceiro);
    
    List<ServicoModel> findByParceiroIdPartner(UUID parceiroId);
    
    List<ServicoModel> findByParceiroAndTipo(PartnerModel parceiro, TipoServico tipo);
    
    List<ServicoModel> findByParceiroAndAceitaPetGrande(PartnerModel parceiro, Boolean aceitaPetGrande);
    
    boolean existsByNomeAndTipoAndParceiro(String nome, TipoServico tipo, PartnerModel parceiro);

}