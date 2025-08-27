package com.redepatas.api.parceiro.dtos.AgedamentoDtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoNotificationDto(
    UUID idAgendamento,
    String nomeCliente,
    String nomePet,
    String imagemPet,
    String servico,
    Double precoFinal,
    LocalDateTime dataPedido,
    LocalDate dataAgendamento,
    String intervalo
) {}