package com.redepatas.api.repositories;

import com.redepatas.api.models.PetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetModel, UUID> {
    List<PetModel> findAllByClient_IdUser(UUID idUser);

}
