package com.redepatas.api.parceiro.models;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "endereco_partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idEndereco;

    private String rua;
    private String cidade;
    private String estado;
    private String bairro;
    private String cep;
    private String complemento;
    private Integer numero;
    private String lugar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partner", nullable = false)
    private PartnerModel partnerModel;

    public EnderecoPartner(String rua, String bairro, String cidade, String estado, String cep, Integer numero,
            String complemento,
            String lugar) {
        this.rua = rua;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
        this.lugar = lugar;
    }
}
