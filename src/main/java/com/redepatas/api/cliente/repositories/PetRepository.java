package com.redepatas.api.cliente.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.PetModel;

@Repository
public interface PetRepository extends JpaRepository<PetModel, UUID> {
    List<PetModel> findAllByClient_IdUser(UUID idUser);

    public boolean existsByRgPet(String rgPet);

}
