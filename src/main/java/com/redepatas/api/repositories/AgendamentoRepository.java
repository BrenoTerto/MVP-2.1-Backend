package com.redepatas.api.repositories;

import com.redepatas.api.models.AgendamentoModel;
import com.redepatas.api.models.ClientModel;
import com.redepatas.api.models.PetModel;
import com.redepatas.api.models.Partner.HorarioIntervaloModel;
import com.redepatas.api.models.Partner.PartnerModel;

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
