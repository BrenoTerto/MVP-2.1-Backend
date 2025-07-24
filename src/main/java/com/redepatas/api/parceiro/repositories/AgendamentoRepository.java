package com.redepatas.api.parceiro.repositories;

import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PetModel;
import com.redepatas.api.parceiro.models.AgendamentoModel;
import com.redepatas.api.parceiro.models.HorarioIntervaloModel;
import com.redepatas.api.parceiro.models.PartnerModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, UUID> {
    List<AgendamentoModel> findByCliente(ClientModel cliente);

    boolean existsByPetModel(PetModel pet);

    boolean existsByPartnerModelAndDataAgendamentoAndIntervalo(
            PartnerModel partnerModel,
            LocalDate dataAgendamento,
            HorarioIntervaloModel intervalo);
}
