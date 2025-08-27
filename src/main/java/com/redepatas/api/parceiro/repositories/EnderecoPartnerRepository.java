package com.redepatas.api.parceiro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redepatas.api.parceiro.models.EnderecoPartner;

import java.util.UUID;

@Repository
public interface EnderecoPartnerRepository extends JpaRepository<EnderecoPartner, UUID> {
}
