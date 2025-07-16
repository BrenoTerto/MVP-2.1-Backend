package com.redepatas.api.repositories;

import com.redepatas.api.models.ClientModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, UUID> {
    UserDetails findByLogin(String login);

    ClientModel findByEmail(String email);

    UserDetails findByPhoneNumber(String phoneNumber);

    UserDetails findByCPF(String cpf);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM users c
                WHERE c.login = :login
            """)
    boolean existsByLogin(@Param("login") String login);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM users c
                WHERE c.CPF = :CPF
            """)
    boolean existsByCPF(@Param("CPF") String CPF);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM users c
                WHERE c.phoneNumber = :phoneNumber OR c.email = :email
            """)
    boolean existsByEmailOuNumero(@Param("phoneNumber") String phoneNumber,
            @Param("email") String email);

}
