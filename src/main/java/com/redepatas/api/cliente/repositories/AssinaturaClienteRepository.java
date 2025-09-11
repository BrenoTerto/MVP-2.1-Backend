package com.redepatas.api.cliente.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.AssinaturaClienteModel;

@Repository
public interface AssinaturaClienteRepository extends JpaRepository<AssinaturaClienteModel, UUID> {

    AssinaturaClienteModel findByIdAsaas(String idSub);

    AssinaturaClienteModel findByCliente_IdUser(UUID idUser);

    @Query(value = """
      SELECT COUNT(*)
      FROM assinaturas_cliente a
      WHERE a.status_assinatura = 'ATIVA'
  """, nativeQuery = true)
    Integer countAssinaturasAtivas();
}
