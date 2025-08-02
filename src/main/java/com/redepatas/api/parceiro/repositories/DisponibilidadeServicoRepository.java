package com.redepatas.api.parceiro.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.redepatas.api.parceiro.models.DisponibilidadeServicoModel;
import com.redepatas.api.parceiro.models.Enum.DiaSemana;

public interface DisponibilidadeServicoRepository extends JpaRepository<DisponibilidadeServicoModel, UUID> {

    List<DisponibilidadeServicoModel> findByServicoIdAndAtivoTrue(UUID servicoId);
    
    List<DisponibilidadeServicoModel> findByParceiroIdAndAtivoTrue(UUID parceiroId);
    
    List<DisponibilidadeServicoModel> findByServicoIdAndDiaSemanaAndAtivoTrue(UUID servicoId, DiaSemana diaSemana);
    
    @Query("SELECT d FROM DisponibilidadeServicoModel d WHERE d.parceiro.id = :parceiroId " +
           "AND d.servico.id = :servicoId AND d.ativo = true")
    List<DisponibilidadeServicoModel> findByParceiroAndServico(
        @Param("parceiroId") UUID parceiroId,
        @Param("servicoId") UUID servicoId
    );
    
    boolean existsByServicoIdAndDiaSemanaAndAtivoTrue(UUID servicoId, DiaSemana diaSemana);

}
