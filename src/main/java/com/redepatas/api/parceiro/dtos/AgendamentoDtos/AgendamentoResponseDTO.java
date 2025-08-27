package com.redepatas.api.parceiro.dtos.AgendamentoDtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class AgendamentoResponseDTO {
    private UUID id;
    private UUID servicoId;
    private UUID parceiroId;
    private UUID petId;
    private UUID horarioId;
    private LocalDate dataAgendamento;
    private Double precoFinal;
    private Double precoBase;
    private Double adicionaisTotal;
    private List<AgendamentoResponseDTO.ItemAdicional> itens;

    @Data
    public static class ItemAdicional {
        private UUID adicionalId;
        private String nome;
        private Double precoAplicado;
    }
}
