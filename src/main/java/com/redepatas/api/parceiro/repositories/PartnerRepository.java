package com.redepatas.api.parceiro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.PartnerModel;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<PartnerModel, UUID> {

    boolean existsByCnpjCpf(String cnpjCpf);

    boolean existsByEmailContato(String email);

    List<PartnerModel> findByEndereco_Cidade(String cidade);

}
