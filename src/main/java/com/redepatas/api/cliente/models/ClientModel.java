package com.redepatas.api.cliente.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.redepatas.api.cliente.controllers.AssinaturaClienteModel;
import com.redepatas.api.infra.security.TokenUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Table(name = "users")
@Entity(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idUser")
public class ClientModel implements UserDetails, TokenUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idUser;
    private String login;
    private String password;

    @NotNull(message = "O nome não pode ser nulo")
    private String name;
    @Enumerated(EnumType.STRING)
    private ClientRole role;
    private String phoneNumber;
    private String email;
    private String photoUrl;
    @Column(unique = true, name = "cpf")
    @Size(min = 14, max = 14, message = "O CPF deve ter 14 dígitos")
    @NotNull(message = "O CPF não pode ser nulo")
    private String CPF;
    private LocalDate birthDate;
    private LocalDateTime registrationDate;
    @OneToMany(mappedBy = "clientModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PetModel> pets = new ArrayList<>();
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AssinaturaClienteModel assinatura;

    @Column(nullable = true)
    private String idCustomer;
    @Column(nullable = false)
    private boolean emailConfirmado = false;

    public ClientModel(String login, String encryptedPassword, String email, String numero, String name,
            ClientRole role, String CPF) {
        this.login = login;
        this.password = encryptedPassword;
        this.name = name;
        this.role = role;
        this.CPF = CPF;
        this.phoneNumber = numero;
        this.email = email;
        this.registrationDate = LocalDateTime.now();
        this.idCustomer = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == ClientRole.ADMIN)
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Implementação da interface TokenUser
    @Override
    public UUID getId() {
        return this.idUser;
    }

    @Override
    public String getNome() {
        return this.name;
    }
}