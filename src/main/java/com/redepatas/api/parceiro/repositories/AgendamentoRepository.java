package com.redepatas.api.parceiro.repositories;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.AgendamentoModel;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, UUID> {
    Optional<AgendamentoModel> findByHorario_IdAndDataAgendamento(UUID horarioId, LocalDate dataAgendamento);
}
