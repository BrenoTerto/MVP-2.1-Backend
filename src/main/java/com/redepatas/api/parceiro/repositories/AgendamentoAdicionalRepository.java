package com.redepatas.api.parceiro.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.AgendamentoAdicionalModel;

@Repository
public interface AgendamentoAdicionalRepository extends JpaRepository<AgendamentoAdicionalModel, UUID> {
}
