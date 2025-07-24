package com.redepatas.api.parceiro.dtos.PartnerDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

import com.redepatas.api.cliente.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.parceiro.models.Enum.TipoPartner;

public record PartnerRecordDto(

        @NotBlank(message = "O nome é obrigatório.") String name,

        String imageUrl,

        @NotBlank(message = "O CNPJ ou CPF é obrigatório.") String cnpjCpf,

        @NotNull(message = "O campo email é obrigatório.") String emailContato,

        @NotNull(message = "O numero de telefone é obrigatório") String numeroContato,

        @NotEmpty(message = "O tipo de animal é obrigatório.") String tipoPet,

        @NotNull(message = "O endereço é obrigatório.") @Valid EnderecoDto endereco,

        @NotNull(message = "A categoria é obrigatória.") TipoPartner categoria,

        List<HorarioFuncionamentoDto> horariosFuncionamento,

        @NotEmpty List<@Valid ServicoDto> servicos,

        String descricao

) {
}
