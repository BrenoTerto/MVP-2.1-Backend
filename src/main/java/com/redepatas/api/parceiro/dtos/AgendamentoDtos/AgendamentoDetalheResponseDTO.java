package com.redepatas.api.parceiro.dtos.AgendamentoDtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;
import com.redepatas.api.parceiro.models.TipoServico;

public record AgendamentoDetalheResponseDTO(
    UUID id,
    LocalDate dataAgendamento,
    String horario,
    StatusAgendamento status,
    Double precoFinal,
    TipoServico servicoTipo,
    String clienteNome,
    String petNome,
    String petEspecie,
    String petRaca,
    String petObservacoes,
    Boolean petCastrado,
    Boolean petSociavel,
    String petSexo,
    BigDecimal petPeso,
    String petTipoSanguineo,
    String petPorte,
    String petAvatarUrl,
    List<ItemAdicional> itens
) {
    public static record ItemAdicional(
        String nome,
        Double precoAplicado
    ) {}
}
