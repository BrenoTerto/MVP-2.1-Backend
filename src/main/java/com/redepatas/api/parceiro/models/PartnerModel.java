package com.redepatas.api.parceiro.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.redepatas.api.parceiro.models.Enum.TipoPartner;
import com.redepatas.api.cliente.models.ClientRole;
import com.redepatas.api.infra.security.TokenUser;

@Table(name = "partner")
@Entity(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPartner")
public class PartnerModel implements UserDetails, TokenUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idPartner;

    @Column(unique = true)
    private String login;
    private String password;
    private ClientRole role;

    private String name;

    private String imageUrl;

    @NotNull(message = "CNPJ/CPF não pode ser nulo")
    private String cnpjCpf;

    private Long avaliacao;
    private Integer qtdAvaliacoes;

    private String emailContato;
    private String numeroContato;

    private String tipoPet;

    @OneToOne(mappedBy = "partnerModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private EnderecoPartner endereco;

    @Enumerated(EnumType.STRING)
    private TipoPartner tipo;

    // @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // private List<Servico> servicos;

    // @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // private List<HorarioFuncionamentoModel> horariosFuncionamento;

    private String descricao;

    public PartnerModel(
            String login,
            String password,
            String name,
            String imageUrl,
            String cnpjCpf,
            String emailContato,
            String numeroContato,
            EnderecoPartner endereco,
            TipoPartner tipo,
            String descricao,
            String tipoPet, // TODOS, GRANDE, PEQUENO
            ClientRole role
    ) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.name = name;
        this.imageUrl = imageUrl;
        this.cnpjCpf = cnpjCpf;
        this.avaliacao = 5L;
        this.qtdAvaliacoes = 1;
        this.emailContato = emailContato;
        this.numeroContato = numeroContato;
        this.endereco = endereco;
        this.tipo = tipo;
        this.tipoPet = tipoPet;
        this.descricao = descricao;
    }

    // Implementação da interface UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PARTNER"));
    }

    @Override
    public String getPassword() {
        return password;
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
    public String getLogin() {
        return login;
    }

    @Override
    public Object getRole() {
        return role;
    }

    @Override
    public UUID getId() {
        return idPartner;
    }
}
