package com.redepatas.api.repositories;

import com.redepatas.api.models.ClientModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, UUID> {
    UserDetails findByLogin(String login);
    UserDetails findByPhoneNumber(String phoneNumber);
    UserDetails findByCPF(String cpf);
}

