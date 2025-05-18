package com.redepatas.api.repositories;

import com.redepatas.api.models.EnderecoPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnderecoPartnerRepository extends JpaRepository<EnderecoPartner, UUID> {
}
