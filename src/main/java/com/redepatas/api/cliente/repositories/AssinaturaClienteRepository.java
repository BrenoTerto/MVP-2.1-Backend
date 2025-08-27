package com.redepatas.api.cliente.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.controllers.AssinaturaClienteModel;

@Repository
public interface AssinaturaClienteRepository extends JpaRepository<AssinaturaClienteModel, UUID> {
  AssinaturaClienteModel findByIdAsaas(String idSub);

}
