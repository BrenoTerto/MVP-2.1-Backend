package com.redepatas.api.cliente.models;

import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "O nome da rua é obrigatório.")
    @Size(max = 255, message = "O nome da rua deve ter no máximo 255 caracteres.")
    private String rua;

    @NotBlank(message = "O nome da cidade é obrigatória.")
    @Size(max = 100, message = "O nome da cidade deve ter no máximo 100 caracteres.")
    private String cidade;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres.")
    private String estado;

    @NotBlank(message = "O nome do bairro é obrigatório.")
    @Size(max = 100, message = "O nome do bairro deve ter no máximo 100 caracteres.")
    private String bairro;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "Formato de CEP inválido. Use XXXXX-XXX.")
    private String cep;
    
    @Size(max = 255, message = "O complemento deve ter no máximo 255 caracteres.")
    private String complemento;

    @NotNull(message = "O número é obrigatório.")
    @Positive(message = "O número deve ser um valor positivo.")
    private Integer numero;

    @Size(max = 100, message = "O campo 'lugar' deve ter no máximo 100 caracteres.")
    private String lugar;

    @NotNull(message = "O status 'selecionado' não pode ser nulo.")
    private Boolean selecionado;

    @NotNull(message = "A associação com o cliente é obrigatória.")
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