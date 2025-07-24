package com.redepatas.api.cliente.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.PetModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetModel, UUID> {
    List<PetModel> findAllByClient_IdUser(UUID idUser);

}
