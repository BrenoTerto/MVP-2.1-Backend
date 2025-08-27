package com.redepatas.api.cliente.models;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "endereco")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

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
    private Boolean selecionado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private ClientModel clientModel;

    public Endereco(String rua, String bairro, String cidade, String estado, String cep, Integer numero,
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
        this.selecionado = true;
    }
}
