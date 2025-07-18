package com.redepatas.api.repositories;

import com.redepatas.api.models.AssinaturaClienteModel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssinaturaClienteRepository extends JpaRepository<AssinaturaClienteModel, UUID> {
  AssinaturaClienteModel findByIdAsaas(String idSub);

  AssinaturaClienteModel findByCliente_IdUser(UUID idUser);
}
