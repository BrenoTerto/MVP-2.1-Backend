package com.redepatas.api.parceiro.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.parceiro.models.AgendamentoModel;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, UUID> {

    List<AgendamentoModel> findByCliente(ClientModel paramClientModel);

    @Query(value = """
        SELECT CASE 
                 WHEN EXISTS (
                   SELECT 1 
                   FROM Agendamento ag
                   WHERE ag.horario_id = :horarioId
                     AND ag.data_agendamento = :dataAgendamento
                     AND ag.status = 'CONFIRMADO'
                 ) THEN true
                 ELSE false
               END
        """, nativeQuery = true)
    boolean existsAgendamentoConfirmado(
            @Param("horarioId") UUID horarioId,
            @Param("dataAgendamento") LocalDate dataAgendamento
    );

    List<AgendamentoModel> findAllByParceiro_IdPartnerAndDataAgendamento(UUID parceiroId, LocalDate dataAgendamento);

    List<AgendamentoModel> findAllByParceiro_IdPartnerAndStatus(UUID parceiroId, StatusAgendamento status);

    Optional<AgendamentoModel> findByIdAndParceiro_IdPartner(UUID id, UUID parceiroId);

    List<AgendamentoModel> findAllByHorario_IdAndDataAgendamento(UUID horarioId, LocalDate dataAgendamento);

    boolean existsByClienteAndStatus(ClientModel cliente, StatusAgendamento status);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AgendamentoModel a "
            + "WHERE a.horario.dia.agenda.servico.id = :servicoId "
            + "AND a.dataAgendamento >= :data "
            + "AND a.status IN :statuses")
    boolean existsFuturosPorServico(@Param("servicoId") UUID servicoId,
            @Param("data") LocalDate data,
            @Param("statuses") List<StatusAgendamento> statuses);
}
