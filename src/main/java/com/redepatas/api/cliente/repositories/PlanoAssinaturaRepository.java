package com.redepatas.api.cliente.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.PlanoAssinatura;

@Repository
public interface PlanoAssinaturaRepository extends JpaRepository<PlanoAssinatura, Long> {
    Optional<PlanoAssinatura> findByNome(String nome);
}
