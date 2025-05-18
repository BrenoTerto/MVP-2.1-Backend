package com.redepatas.api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vacinas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idVacina")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idVacina;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataAplicacao;
}