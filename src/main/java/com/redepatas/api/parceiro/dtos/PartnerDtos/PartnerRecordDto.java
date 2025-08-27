package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.redepatas.api.cliente.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.parceiro.models.Enum.TipoPartner;

public record PartnerRecordDto(

    @Pattern(regexp = "(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})", message = "O login deve ser um CPF (apenas números ou formato XXX.XXX.XXX-XX) ou CNPJ (formato XX.XXX.XXX/XXXX-XX).") @NotBlank(message = "O login é obrigatório.") String login,

    @NotBlank(message = "O campo password é obrigatória.") String password,

    @NotBlank(message = "O nome é obrigatório.") String name,

    @NotNull(message = "O campo email é obrigatório.") String emailContato,

    @NotNull(message = "O numero de telefone é obrigatório") String numeroContato,

    @NotEmpty(message = "O tipo de animal é obrigatório.") String tipoPet,

    @NotNull(message = "O endereço é obrigatório.") @Valid EnderecoDto endereco,

    @NotNull(message = "A categoria é obrigatória.") TipoPartner categoria,

    String descricao

) {
}
