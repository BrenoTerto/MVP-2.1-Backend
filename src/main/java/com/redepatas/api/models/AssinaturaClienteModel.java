package com.redepatas.api.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.redepatas.api.models.Enum.StatusAssinatura;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "assinaturas_cliente")
public class AssinaturaClienteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    private ClientModel cliente;

    @ManyToOne
    private PlanoAssinatura plano;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
    
    private String idAsaas;
    @Enumerated(EnumType.STRING)
    private StatusAssinatura statusAssinatura;
}
