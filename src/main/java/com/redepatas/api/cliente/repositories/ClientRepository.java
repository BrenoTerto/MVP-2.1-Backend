package com.redepatas.api.cliente.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.redepatas.api.cliente.models.ClientModel;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, UUID> {
    UserDetails findByLogin(String login);

    UserDetails findByPhoneNumber(String phoneNumber);

    UserDetails findByCPF(String cpf);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM users c
                WHERE c.login = :login OR c.phoneNumber = :phoneNumber OR c.email = :email
            """)
    boolean existsByLoginOrPhoneNumberOrCPF(@Param("login") String login, @Param("phoneNumber") String phoneNumber,
            @Param("email") String email);

}
