package com.redepatas.api.parceiro.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.dtos.PartnerDtos.AdicionalProjecao;
import com.redepatas.api.parceiro.models.AdicionaisModel;

@Repository
public interface AdicionaisRepository extends JpaRepository<AdicionaisModel, UUID> {

    @Query("SELECT a FROM AdicionaisModel a WHERE a IN (SELECT ad FROM ServicoModel s JOIN s.adicionais ad WHERE s.id = :servicoId)")
    List<AdicionaisModel> findByServicoId(@Param("servicoId") UUID servicoId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AdicionaisModel a WHERE a.nome = :nome AND a IN (SELECT ad FROM ServicoModel s JOIN s.adicionais ad WHERE s.id = :servicoId)")
    boolean existsByNomeAndServicoId(@Param("nome") String nome, @Param("servicoId") UUID servicoId);

    @Query(value = "SELECT id, nome, descricao, preco_grande, preco_pequeno FROM adicionais WHERE servico_id = :servicoId", nativeQuery = true)
    List<AdicionalProjecao> findAdicionaisByServicoId(@Param("servicoId") UUID servicoId);
}
