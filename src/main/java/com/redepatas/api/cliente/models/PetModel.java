package com.redepatas.api.cliente.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "pets")
@Entity(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idPet;
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private ClientModel client;
    @Column(unique = true)
    private String rgPet;
    private String avatarUrl;
    @NotNull(message = "O campo nome é obrigatório.")
    @Column(nullable = false)
    private String nome;
    @NotNull(message = "O campo espécie é obrigatório.")
    @Column(nullable = false)
    private String especie;
    private String raca;
    private String observacoes;
    @NotNull(message = "O campo castrado é obrigatório.")
    @Column(nullable = false)
    private Boolean castrado;
    private Boolean sociavel;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pet_id")
    private List<Vacina> vacinas;
    private LocalDate dataNascimento;
    @NotNull(message = "O campo sexo é obrigatório.")
    @Column(nullable = false)
    private String sexo;
    private BigDecimal peso;
    private String tipoSanguineo;
    private String porte;

}
