package com.redepatas.api.parceiro.repositories;

import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.AgendamentoModel;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, UUID> {
    Optional<AgendamentoModel> findByHorario_IdAndDataAgendamento(UUID horarioId, LocalDate dataAgendamento);

    List<AgendamentoModel> findAllByParceiro_IdPartnerAndDataAgendamento(UUID parceiroId, LocalDate dataAgendamento);

    List<AgendamentoModel> findAllByParceiro_IdPartnerAndStatus(UUID parceiroId, StatusAgendamento status);

    Optional<AgendamentoModel> findByIdAndParceiro_IdPartner(UUID id, UUID parceiroId);

    List<AgendamentoModel> findAllByHorario_IdAndDataAgendamento(UUID horarioId, LocalDate dataAgendamento);
}
