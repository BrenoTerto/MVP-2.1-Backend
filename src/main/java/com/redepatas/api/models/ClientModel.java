package com.redepatas.api.models;

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
public class ClientModel implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idUser;
    private String login;
    private String password;
    @Column(unique = true)
    @Size(min = 14, max = 14, message = "O CPF deve ter 14 dígitos")
    @NotNull(message = "O CPF não pode ser nulo")
    private String CPF;
    @NotNull(message = "O nome não pode ser nulo")
    private String name;
    private ClientRole role;
    private String phoneNumber;
    private String email;
    private String photoUrl;
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

    public ClientModel(String login, String encryptedPassword, String CPF, String name, ClientRole role) {
        this.login = login;
        this.password = encryptedPassword;
        this.CPF = CPF;
        this.name = name;
        this.role = role;
        this.registrationDate = LocalDateTime.now();
        this.idCustomer = null;
        if (isEmail(login)) {
            this.email = login;
        } else if (isTelefone(login)) {
            this.phoneNumber = login;
        } else {
            throw new IllegalArgumentException("Login deve ser um e-mail ou telefone válido.");
        }
    }

    private boolean isEmail(String login) {
        return login.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isTelefone(String login) {
        return login.matches("^\\d{10,11}$"); 
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
}