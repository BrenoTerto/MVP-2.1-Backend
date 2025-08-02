package com.redepatas.api.parceiro.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.redepatas.api.parceiro.models.AgendamentoModel;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, UUID> {

    List<AgendamentoModel> findByParceiroIdAndDataAgendamento(UUID parceiroId, LocalDate dataAgendamento);
    
    List<AgendamentoModel> findByParceiroIdAndStatus(UUID parceiroId, StatusAgendamento status);
    
    List<AgendamentoModel> findByClienteId(UUID clienteId);
    
    List<AgendamentoModel> findByServicoId(UUID servicoId);
    
    @Query("SELECT a FROM AgendamentoModel a WHERE a.parceiro.id = :parceiroId " +
           "AND a.dataAgendamento = :data " +
           "AND a.status != 'CANCELADO' " +
           "AND ((a.horarioInicio < :horarioFim AND a.horarioFim > :horarioInicio))")
    List<AgendamentoModel> findConflitosHorario(
        @Param("parceiroId") UUID parceiroId,
        @Param("data") LocalDate data,
        @Param("horarioInicio") LocalTime horarioInicio,
        @Param("horarioFim") LocalTime horarioFim
    );
    
    @Query("SELECT a FROM AgendamentoModel a WHERE a.parceiro.id = :parceiroId " +
           "AND a.dataAgendamento BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY a.dataAgendamento, a.horarioInicio")
    List<AgendamentoModel> findByParceiroAndPeriodo(
        @Param("parceiroId") UUID parceiroId,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

}
