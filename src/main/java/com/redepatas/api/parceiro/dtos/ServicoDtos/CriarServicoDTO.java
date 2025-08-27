package com.redepatas.api.parceiro.dtos.ServicoDtos;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CriarServicoDTO {

    private UUID parceiroId;

    private String descricao;

    @NotBlank(message = "Tipo do serviço é obrigatório")
    private String tipo;

    @NotNull(message = "Preço pequeno é obrigatório")
    @Positive(message = "Preço pequeno deve ser maior que zero")
    private Double precoPequeno;

    private Double precoGrande;

    private Boolean aceitaPetGrande = true;

    @Valid
    private List<CriarAdicionalDTO> adicionais;

    @Valid
    @NotNull(message = "A agenda do serviço é obrigatória")
    private CriarAgendaDTO agenda;
}
